# Chartographer

From **charta** (*Latin*) or **harta** — one of the names for [papyrus](https://en.wikipedia.org/wiki/Papyrus).

You need to implement a service called **Chartographer** — a service for reconstructing images of ancient scrolls and papyri.  
The images are raster-based and created in stages (as separate fragments).  
The reconstructed image can be retrieved in fragments (even if only partially reconstructed).

It is expected that many researchers will use this service simultaneously.

## HTTP API

You need to implement 4 HTTP methods:

```
POST /chartas/?width={width}&height={height}
```
Creates a new papyrus image of the specified size (in pixels),  
where `{width}` and `{height}` are positive integers not exceeding `20,000` and `50,000`, respectively.  
Request body: empty.  
Response body: `{id}` — a unique string identifier of the image.  
Response code: `201 Created`.

```
POST /chartas/{id}/?x={x}&y={y}&width={width}&height={height}
```
Saves a reconstructed image fragment of size `{width} x {height}` at coordinates `({x};{y})`.  
The coordinates refer to the position of the top-left corner of the fragment relative to the top-left corner of the entire image.  
In other words, the top-left corner of the image is the origin `(0;0)`.  
Request body: image in `BMP` format (RGB color, 24 bits per pixel).  
Response body: empty.  
Response code: `200 OK`.

```
GET /chartas/{id}/?x={x}&y={y}&width={width}&height={height}
```
Retrieves a reconstructed part of the image of size `{width} x {height}` at coordinates `({x};{y})`,  
where `{width}` and `{height}` are positive integers not exceeding 5,000.  
The coordinates refer to the position of the top-left corner of the fragment relative to the top-left corner of the entire image.  
In other words, the top-left corner of the image is the origin `(0;0)`.  
Response body: image in `BMP` format (RGB color, 24 bits per pixel).  
Response code: `200 OK`.

```
DELETE /chartas/{id}/
```
Deletes the image with the identifier `{id}`.  
Request and response bodies: empty.  
Response code: `200 OK`.

### Error Handling

1. Requests for a nonexistent image `{id}` must return `404 Not Found`.
2. Requests with invalid `{width}` or `{height}` parameters must return `400 Bad Request`.
3. Requests for fragments that do not overlap with the image in terms of coordinates must return `400 Bad Request`.  
However, fragments may *partially* lie outside the image boundaries (see notes) — such requests are considered valid.

### Notes

1. Image dimensions do not exceed `20,000 x 50,000`.
2. Some images are guaranteed to not fit entirely in memory.  
You must support storing data on disk.
3. Image format — [BMP](https://en.wikipedia.org/wiki/BMP_file_format). Color in RGB (no alpha channel), 24 bits per pixel.
4. If a new fragment overlaps a previously restored area, the new fragment is always applied.
5. If a requested fragment includes parts that are not yet reconstructed, the unreconstructed areas must be filled with black.  
Similarly, areas outside the image boundaries must also be filled with black (see example below).
6. If a fragment being saved extends beyond the image boundaries, the out-of-bounds portion is ignored.  
Example: image size is `50 x 100`, fragment size is `50 x 50`, and its top-left corner is at `(25;25)`.  
The right half of the fragment is ignored. Schematic representation:

```
╔═════════╗
║         ║
║    ┌────╫────┐
║    │    ║    │
║    │    ║    │
║    │    ║    │
║    └────╫────┘
║         ║
╚═════════╝
```

The implementation is expected to work correctly in edge cases and handle errors gracefully.  
It is strongly recommended to cover the core functionality with unit tests.

## Requirements for Implementation

- Code must compile and run with Java 11.
- Build the service using Apache Maven with the command `mvn package`.
- Unit tests must be executed during the build.
- The service should be packaged as a fat JAR — all dependencies included in a single JAR file.
- The service is run with the command `java -jar chartographer-1.0.0.jar /path/to/content/folder` inside the project's `target` directory,  
  where `/path/to/content/folder` is the directory used by the service to store data.
- The service must accept HTTP requests on the standard port (`8080`).
- Source code must follow [Java Code Conventions](https://www.oracle.com/technetwork/java/codeconventions-150003.pdf)  
  and the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).

## Testing Information

The service will be run in Docker on a *multi-core* machine.
