package org.example;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Main {

    private static Map<String, String> getLanguageOptions() {
        Map<String, String> options = new HashMap<>();
        options.put("js.webassembly", "true");
        options.put("js.commonjs-require", "true");
        return options;
    }

    public static void main(String[] args) throws IOException {

        Context context = Context.newBuilder("js","wasm")
                .options(getLanguageOptions())
                .allowAllAccess(true)
                .build();
        byte[] wasmBinary  = Files.readAllBytes(Paths.get("src/main/resources/ort-wasm.wasm"));
        context.getBindings("js").putMember("modelWasmBuffer",wasmBinary);


        context.eval("js","""
                if (typeof performance === 'undefined') {
                  globalThis.performance = {
                    now: () => Date.now()
                  };
                }
                globalThis.self = globalThis;
                """);
        context.eval(Source.newBuilder("js", Objects.requireNonNull(Main.class.getResource("/ort.js")))
                .build());
        byte[] modelData = Files.readAllBytes(Paths.get("src/main/resources/house_price_model.onnx"));
        context.getBindings("js").putMember("modelBuffer",modelData);

        context.eval(Source.newBuilder("js", Objects.requireNonNull(Main.class.getResource("/script.js")))
                .build());

    }
}