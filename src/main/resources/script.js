
let modelBuffer;
(async (modelBuffer) => {
    ort.env.wasm.numThreads = 2;
ort.env.wasm.proxy = true;
ort.env.wasm.wasmPaths = 'node_modules/onnxruntime-web/dist/';



  try {
    const session = await ort.InferenceSession.create(new Uint8Array(globalThis.modelBuffer));
    console.log("session created")
    const inputValues = [3, 10, 5, 2, 1300]; // Example:
                                             // 3 rooms, 10 years old, 5km from city, 2 bathrooms, 1200 sqft

    const inputTensor = new ort.Tensor("float32", Float32Array.from(inputValues), [1, 5]);

    const feeds = { input: inputTensor };
    const results = await session.run(feeds);
    console.log(`Model prediction: ${results.output.data}`);
  } catch (e) {
    console.error("Failed to run model:", e);
  }
})();


