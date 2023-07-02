rootProject.name = "ankh-core"
include("api")

include("aloader")

include("loader:logger")
include("loader:cloud")
include("loader:aksupport")

// include("test-plugin")
include("gradle-plugin")

include("libs:shadow-bsh")
include("libs:shadow-spring-boot-loader")
include("libs:shadow-paper-lib")
include("libs:shadow-callsite-nbt")

include("services:service-groovy")
include("services:service-js-nashorn")
include("services:service-kether")
include("services:service-neigeitems")
