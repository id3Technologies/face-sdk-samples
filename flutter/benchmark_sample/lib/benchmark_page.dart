import 'package:benchmark_sample/targeted_page.dart';
import 'package:flutter/material.dart';

import 'complete_page.dart';

class BenchmarkPage extends StatefulWidget {
  const BenchmarkPage({Key? key}) : super(key: key);

  @override
  State<BenchmarkPage> createState() => _BenchmarkPageState();
}

class _BenchmarkPageState extends State<BenchmarkPage> {
  Stream<String>? completeBenchmarkStream;
  bool? targetedBenchmark;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Benchmark Sample"),
      ),
      body: Column(
        children: [
          // const RepaintBoundary(child:  CircularProgressIndicator()),
          //CircularProgressIndicator(),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: [
              SizedBox(
                width: 160,
                child: ElevatedButton(
                  onPressed: () {
                    setState(() {
                      targetedBenchmark = false;
                    });
                  },
                  child: const Text(
                    "COMPLETE BENCHMARK",
                    textAlign: TextAlign.center,
                  ),
                ),
              ),
              SizedBox(
                width: 160,
                child: ElevatedButton(
                  onPressed: () {
                    setState(() {
                      targetedBenchmark = true;
                    });
                  },
                  child: const Text(
                    "TARGETED BENCHMARK",
                    textAlign: TextAlign.center,
                  ),
                ),
              ),
            ],
          ),
          Expanded(
            child: SingleChildScrollView(
              child: targetedBenchmark != null
                  ? targetedBenchmark == true
                      ? const TargetedPage()
                      : const CompletePage()
                  : Container(),
            ),
          ),
        ],
      ),
    );
  }
}
