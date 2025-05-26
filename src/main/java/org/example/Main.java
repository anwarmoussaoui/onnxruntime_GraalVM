package org.example;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    private static Map<String, String> getLanguageOptions() {
        Map<String, String> options = new HashMap<>();

        options.put("js.ecmascript-version", "2023");
        options.put("js.top-level-await", "true");
        options.put("js.webassembly", "true");
        options.put("js.commonjs-require", "true");
        options.put("js.mle-mode", "true");
        options.put("js.esm-eval-returns-exports", "true");
        options.put("js.unhandled-rejections", "throw");
        options.put("js.commonjs-require-cwd", Paths.get("./src/main/resources").toAbsolutePath().toString());
        return options;
    }

    public static void main(String[] args) throws IOException {

        Context context = Context.newBuilder("js","wasm")
                .options(getLanguageOptions())
                .allowHostAccess(HostAccess.ALL)
                .allowIO(true)
                .option("engine.WarnInterpreterOnly", "false")
                .option("js.esm-eval-returns-exports", "true")
                .option("js.unhandled-rejections", "throw")
                .allowAllAccess(true)
                .allowHostClassLookup(s -> true)
                .build();
        byte[] wasmBinary  = Files.readAllBytes(Paths.get("src/main/resources/ort-wasm.wasm"));
        context.getBindings("js").putMember("modelWasmBuffer",wasmBinary);


        context.eval("js","""
                if (typeof performance === 'undefined') {
                  globalThis.performance = {
                    now: () => Date.now()
                  };
                }
                
                
                globalThis.Blob = function Blob(content, options) {
                console.log("blob is used");
                  const buffer = Buffer.from(content[0]);
                  return { buffer };
                };
                
                globalThis.URL = {
                  createObjectURL: (blob) => {
                    const tmpPath = '/tmp/tmp-worker.js';
                    require('fs').writeFileSync(tmpPath, blob.buffer);
                    return tmpPath;
                  }
                };
                
                globalThis.self = globalThis;
                
                globalThis.process = {
                  env: {},         // Add your env vars here, e.g., { NODE_ENV: "production" }
                  argv: [],
                  cwd: () => '/',
                  nextTick: (cb) => Promise.resolve().then(cb)
                };
                globalThis.fetch = async function(url) {
                
                  console.log("Intercepted fetch:", url);
                  return {
                    arrayBuffer: async () => {
                      return new Uint8Array(modelWasmBuffer); \
                    }
                  };
                };

               
                
                """);


        context.eval(Source.newBuilder("js",Main.class.getResource("/ort.js"))
                .build());
        byte[] modelData = Files.readAllBytes(Paths.get("src/main/resources/linear_model.onnx"));
        context.getBindings("js").putMember("modelBuffer",modelData);

        context.eval(Source.newBuilder("js",Main.class.getResource("/script.js"))
                .build());

    }
}