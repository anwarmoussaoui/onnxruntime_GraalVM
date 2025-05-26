
let modelBuffer;
(async (modelBuffer) => {
    ort.env.wasm.numThreads = 2;
ort.env.wasm.proxy = true;
ort.env.wasm.wasmPaths = 'node_modules/onnxruntime-web/dist/';



  try {
    const session = await ort.InferenceSession.create(new Uint8Array(globalThis.modelBuffer));
    console.log("session created")
    const inputTensor = new ort.Tensor('float32', new Float32Array([2.0]), [1, 1]);

    const feeds = { input: inputTensor };
    const results = await session.run(feeds);
    console.log(`Model prediction: ${results.output.data}`);
  } catch (e) {
    console.error("Failed to run model:", e);
  }
})();


