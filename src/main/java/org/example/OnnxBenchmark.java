package org.example;

import org.openjdk.jmh.annotations.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class OnnxBenchmark {

    private OnnxRunner runner;
    private byte[] modelData;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        runner = new OnnxRunner();
        modelData = Files.readAllBytes(Paths.get("src/main/resources/house_price_model.onnx"));
    }

    @Benchmark
    public void benchmarkPredict() {
        runner.predict(modelData);
    }
}
