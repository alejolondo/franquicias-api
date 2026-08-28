# API de Franquicias

API REST reactiva para la gestión de franquicias, sus sucursales y los productos ofertados en cada una. Construida con Spring Boot WebFlux, arquitectura limpia y persistencia en MongoDB este ultimo desplegado en MongoAtlas.

**Aplicación desplegada en instancia de AWS:** http://3.214.81.52:8080  


Proyecto de prueba técnica

---

## Tabla de contenido

- [Descripción del dominio](#descripción-del-dominio)
- [Stack tecnológico](#stack-tecnológico)
- [Arquitectura](#arquitectura)
- [Modelo de datos](#modelo-de-datos)
- [Endpoints](#endpoints)
- [Manejo de errores](#manejo-de-errores)
- [Ejecución local](#ejecución-local)
- [Ejecución con Docker](#ejecución-con-docker)
- [Pruebas](#pruebas)


---

## Descripción del dominio

Una **franquicia** tiene un nombre y una lista de **sucursales**. Cada sucursal tiene un nombre y una lista de **productos**. Cada producto tiene un nombre y una cantidad de stock.

La franquicia es la raíz del agregado: todas las operaciones sobre sucursales y productos se realizan a través de ella, lo que garantiza la consistencia del conjunto en cada escritura.

---

## Stack tecnológico

| Componente | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 4.1.1 |
| Modelo de concurrencia | Spring WebFlux (Project Reactor) |
| Estilo de endpoints | Functional endpoints (RouterFunction + Handler) |
| Persistencia | Spring Data Reactive MongoDB |
| Base de datos | MongoDB Atlas (M0) |
| Build | Gradle 9.5.1 |
| Reducción de boilerplate | Lombok |
| Validación | Jakarta Bean Validation |
| Documentación | springdoc-openapi |
| Pruebas | JUnit 5, AssertJ, Mockito, Reactor Test, ArchUnit |
| Contenedores | Docker (build multi-stage) |
| Nube | AWS EC2 + Amazon ECR |

---

## Arquitectura

El proyecto implementa **arquitectura limpia / hexagonal**, con la regla de dependencia apuntando siempre hacia el centro: las capas externas conocen a las internas, nunca al revés.

```
┌─────────────────────────────────────────────────────┐
│  infrastructure                                     │
│  ┌───────────────────┐      ┌────────────────────┐  │
│  │  entrypoint.rest  │      │ drivenadapter.mongo│  │
│  │  Router, Handlers │      │ Documents, Adapter │  │
│  │  DTOs, Mappers    │      │ Mapper             │  │
│  └─────────┬─────────┘      └──────────┬─────────┘  │
│            │                            │            │
│            ▼                            ▼            │
│  ┌───────────────────────────────────────────────┐  │
│  │  usecase                                      │  │
│  │  FranquiciaUseCase, SucursalUseCase,          │  │
│  │  ProductoUseCase                              │  │
│  └───────────────────┬───────────────────────────┘  │
│                      ▼                               │
│  ┌───────────────────────────────────────────────┐  │
│  │  domain                                       │  │
│  │  model/     Franquicia, Sucursal, Producto    │  │
│  │  gateway/   FranquiciaRepository (puerto)     │  │
│  │  exception/ Excepciones de negocio            │  │
│  └───────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

### Estructura de paquetes

```
src/main/java/com/franquicias/api/
├── FranquiciasApiApplication.java
├── domain/                          Java puro. Sin Spring, sin MongoDB.
│   ├── model/                       Entidades inmutables con lógica de negocio
│   ├── gateway/                     Puertos (interfaces)
│   └── exception/                   Excepciones de negocio
│   └── usecase/                   Orquestación. Depende solo del domain
├── infrastructure/
│   ├── entrypoint/rest/             Adaptador de entrada (HTTP)
│   │   ├── RouterRest.java
│   │   ├── ProductoHandler.java
│   │   ├── SucursalHandler.java
│   │   ├── FranquiciaHandler.java
│   │   ├── dto/
│   │   ├── mapper/
│   │   └── exception/
│   └── drivenadapter/mongo/         Adaptador de salida (persistencia)
│       ├── document/
│       ├── mapper/
│       ├── FranquiciaMongoRepository.java
│       └── FranquiciaRepositoryAdapter.java
└── config/                          
    ├── UseCaseConfig.java
```

### Principios aplicados

**El dominio no conoce ningún framework.** Las clases de `domain` y `usecase` no tienen anotaciones de Spring ni de MongoDB. Los casos de uso son clases planas que reciben el puerto por constructor y se registran como beans desde `UseCaseConfig`. Esto permite probarlos sin levantar el contexto de Spring.

**Inversión de dependencias.** El dominio define la interfaz `FranquiciaRepository`; la infraestructura la implementa. Cambiar MongoDB por otro motor de persistencia no requiere tocar una sola línea del dominio.

**Doble mapeo intencional.** `Franquicia` (dominio) y `FranquiciaDocument` (MongoDB) son clases distintas, con un mapper entre ellas. Hoy son similares, pero responden a necesidades diferentes: el documento a requisitos de almacenamiento (índices, embebido, nombre de colección), el modelo a reglas de negocio. Lo mismo aplica entre el dominio y los DTOs de la capa REST.

**Lógica de negocio en el dominio.** El cálculo del producto con mayor stock por sucursal vive en `Franquicia.productosConMayorStockPorSucursal()`, no en el caso de uso ni en el handler. Los casos de uso solo orquestan.

**Validación automatizada de la arquitectura.** La clase `ArchitectureTest` usa ArchUnit para verificar en cada build que ninguna capa viole la regla de dependencia. Si alguien importa Spring dentro del dominio, el build falla.

---

## Modelo de datos

Se optó por **documento embebido**: una única colección `franquicias`, donde cada documento contiene sus sucursales y cada sucursal sus productos.

```json
{
  "_id": "550e8400-e29b-41d4-a716-446655440000",
  "nombre": "Mi Franquicia",
  "sucursales": [
    {
      "id": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
      "nombre": "Sucursal Centro",
      "productos": [
        { "id": "6ba7b811-9dad-11d1-80b4-00c04fd430c8", "nombre": "Café", "stock": 50 },
        { "id": "6ba7b812-9dad-11d1-80b4-00c04fd430c8", "nombre": "Té", "stock": 120 }
      ]
    }
  ]
}
```

**Por qué embebido y no colecciones separadas:**

- Los datos siempre se consultan juntos: no existe el caso de uso "consultar una sucursal sin su franquicia".
- El endpoint del producto con mayor stock por sucursal se resuelve leyendo un solo documento, sin agregaciones ni joins.
- La franquicia es la frontera de consistencia natural del agregado.
- El límite de 16 MB por documento en MongoDB es holgado para el volumen esperado.

El campo `nombre` de la franquicia tiene un índice único, lo que refuerza a nivel de base de datos la regla de negocio que también se valida en el caso de uso.

Sucursales y productos tienen identificadores propios (UUID) en lugar de identificarse por nombre. Esto permite renombrarlos sin cambiar la identidad del recurso ni las URLs.

---

## Endpoints

Base: `/api/v1/franquicias`

| Código | HTTP | Situación |
|---|---|---|
| `VALIDATION_ERROR` | 400 | Datos de entrada inválidos. El arreglo `detalles` lista cada violación |
| `INVALID_STOCK` | 400 | Stock negativo o nulo |
| `FRANQUICIA_NOT_FOUND` | 404 | La franquicia no existe |
| `SUCURSAL_NOT_FOUND` | 404 | La sucursal no existe |
| `PRODUCTO_NOT_FOUND` | 404 | El producto no existe |
| `DUPLICATE_RESOURCE` | 409 | Ya existe un recurso con ese nombre |
| `INTERNAL_ERROR` | 500 | Error no controlado |

Endpoints operativos:

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/actuator/health` | Estado de la aplicación |
| GET | `/v3/api-docs` | Especificación OpenAPI |
| GET | `/webjars/swagger-ui/index.html` | Documentación interactiva |

### Ejemplos

**Crear una franquicia**

```bash
curl -X POST http://3.214.81.52:8080/api/v1/franquicias \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Mi Franquicia"}'
```

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "nombre": "Mi Franquicia",
  "sucursales": []
}
```

**Agregar un producto**

```bash
curl -X POST http://3.214.81.52:8080/api/v1/franquicias/{franquiciaId}/sucursales/{sucursalId}/productos \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Café", "stock": 50}'
```

**Producto con mayor stock por sucursal**

```bash
curl http://3.214.81.52:8080/api/v1/franquicias/{franquiciaId}/productos/mayor-stock
```

```json
[
  {
    "sucursalId": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
    "sucursalNombre": "Sucursal Centro",
    "productoId": "6ba7b812-9dad-11d1-80b4-00c04fd430c8",
    "productoNombre": "Té",
    "stock": 120
  }
]
```

Cada elemento indica a qué sucursal pertenece el producto. Las sucursales sin productos se omiten del resultado.



## Manejo de errores

Todas las respuestas de error siguen el mismo formato:

```json
{
  "timestamp": "2026-08-28T16:30:00.000Z",
  "status": 404,
  "codigo": "FRANQUICIA_NOT_FOUND",
  "mensaje": "No existe franquicia con id: abc123",
  "detalles": [],
  "path": "/api/v1/franquicias/abc123"
}
```

| Código | HTTP | Situación |
|---|---|---|
| `VALIDATION_ERROR` | 400 | Datos de entrada inválidos. El arreglo `detalles` lista cada violación |
| `INVALID_STOCK` | 400 | Stock negativo o nulo |
| `FRANQUICIA_NOT_FOUND` | 404 | La franquicia no existe |
| `SUCURSAL_NOT_FOUND` | 404 | La sucursal no existe |
| `PRODUCTO_NOT_FOUND` | 404 | El producto no existe |
| `DUPLICATE_RESOURCE` | 409 | Ya existe un recurso con ese nombre |
| `INTERNAL_ERROR` | 500 | Error no controlado |

El mapeo de excepciones de dominio a códigos HTTP se realiza en `GlobalErrorAttributes`, dentro de la capa de infraestructura. El dominio lanza excepciones de negocio sin conocer nada sobre HTTP.

---

## Ejecución local

### Requisitos

- Java 17 (JDK)
- MongoDB en local, o una cadena de conexión a MongoDB Atlas
- Git

Gradle no es necesario: el proyecto incluye el wrapper.

### Clonar

```bash
git clone https://github.com/alejolondo/franquicias-api.git
cd franquicias-api
```

### Configurar

La aplicación se configura por variables de entorno. Los valores por defecto apuntan a un MongoDB local.

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `SERVER_PORT` | Puerto HTTP | `8080` |
| `MONGO_URI` | Cadena de conexión a MongoDB | `mongodb://localhost:27017` |
| `MONGO_DATABASE` | Nombre de la base de datos | `franquicias` |
| `LOG_LEVEL` | Nivel de log de la aplicación | `INFO` |

Copiar la plantilla y ajustar:

```bash
cp .env.example .env
```

### Ejecutar

Con MongoDB local en el puerto 27017, no se requiere configuración adicional:

```bash
./gradlew bootRun
```

En Windows:

```powershell
.\gradlew bootRun
```

Contra MongoDB Atlas:

```bash
export MONGO_URI="mongodb+srv://usuario:password@cluster.mongodb.net/franquicias?retryWrites=true&w=majority"
export MONGO_DATABASE=franquicias
./gradlew bootRun
```

La aplicación queda disponible en `http://localhost:8080`.

### Compilar

```bash
./gradlew clean build
```

Genera el artefacto ejecutable en `build/libs/franquicias-api-1.0.0.jar`.

---

## Ejecución con Docker

### Construir la imagen

```bash
docker build -t franquicias-api .
```

El `Dockerfile` usa un build multi-stage: la primera etapa compila con JDK, la segunda ejecuta con JRE. El contenedor corre bajo un usuario sin privilegios y la JVM está configurada con `MaxRAMPercentage` para respetar los límites de memoria del contenedor.

### Ejecutar

```bash
docker run -d \
  --name franquicias-api \
  -p 8080:8080 \
  -e MONGO_URI="mongodb+srv://usuario:password@cluster.mongodb.net/franquicias" \
  -e MONGO_DATABASE=franquicias \
  franquicias-api
```

### Con docker-compose

Levanta la API junto con un MongoDB local, sin necesidad de credenciales externas:

```bash
docker compose up --build
```

Para detener y eliminar los volúmenes:

```bash
docker compose down -v
```

---

## Pruebas

```bash
./gradlew test
```

El reporte HTML queda en `build/reports/tests/test/index.html`.

### Estrategia

| Nivel | Alcance | Herramientas |
|---|---|---|
| Dominio | Reglas de negocio, inmutabilidad, casos de borde | JUnit 5, AssertJ |
| Casos de uso | Orquestación y propagación de errores | Mockito, Reactor Test (`StepVerifier`) |
| Arquitectura | Cumplimiento de la regla de dependencia | ArchUnit |

Las pruebas de dominio y casos de uso no levantan el contexto de Spring ni requieren base de datos: el repositorio se sustituye por un mock del puerto. La suite completa se ejecuta en segundos.

`ArchitectureTest` verifica que:

- El dominio no dependa de Spring ni de MongoDB
- El dominio no dependa de la infraestructura
- Los casos de uso no dependan de Spring ni de la infraestructura
- La capa REST no acceda directamente al adaptador de persistencia

---

## Despliegue en AWS

### Arquitectura del despliegue

```
Cliente HTTP
     │
     ▼
Amazon EC2 (t3.micro, Amazon Linux 2023)
  └── Docker
       └── Contenedor franquicias-api :8080
                │
                ▼
        MongoDB Atlas (M0, us-east-1)
```


---

## Autor

**Alejandro Londoño**

Prueba técnica — Desarrollador Backend