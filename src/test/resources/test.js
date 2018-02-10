// print some logger message
logger.info("testing 123");

// set an import
exports.get("test").put("Hello world");

// use a custom binding
testCallback.run();

// test a pointer
var specialList = exports.pointer("test");
logger.info("a pointer to test: " + specialList());
