# Rate limiting example project

## Purpose
Provide a Java Spring library for rate-limiting HTTP requests on a per-user basis. The user must be identifiable by some sort of key, and that key must be part of the request. There are no fixed requirements on where in the request that key lives, or its type.

The idea is that the library is included as a project dependency, most likely in a project that's running an HTTP server. Autoconfiguration is provided, and details on how to use this in a project are below. 

## Decisions and assumptions
I've chosen to use Spring Boot for this project because: 
  - I am familiar with it, so it is the smallest language/technology barrier between my thoughts and their execution.
  - It simplifies the configuration required to launch a server and see the module working. 
  - There will be a single server running the module, rather than a cluster. This simplifies both the module itself and its reference implementation. 
  - There should be able to accept concurrent requests from the same user and still respect the set rate limit. 
  - xxx explain time - UTC timeline, no timezone awareness

## Reference implementation
xxx token bucket with window, reset behaviour
xxx strenghts and weaknesses 

## Exending the module 
xxx how to add other algos

## Usage
xxx how to build the lib
xxx running tests (and what's tested. Properties?)
xxx how to include in a Boot project
xxx configuration options

## Exercising the module
xxx some bash script to spam the server and see the 429 msgs
