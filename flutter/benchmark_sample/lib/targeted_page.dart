import 'package:flutter/material.dart';

import 'benchmarks.dart';

import 'benchmark_logger.dart';

class TargetedPage extends StatefulWidget {
  const TargetedPage({Key? key}) : super(key: key);

  @override
  _TargetedPageState createState() => _TargetedPageState();
}

class _TargetedPageState extends State<TargetedPage> {
  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        BenchmarkSection(
          benchName: "DETECTOR 3A BENCHMARK",
          benchmark: detector3ABenchmark,
        ),
        BenchmarkSection(
          benchName: "DETECTOR 3B BENCHMARK",
          benchmark: detector3BBenchmark,
        ),
        BenchmarkSection(
          benchName: "QUALITY BENCHMARK",
          benchmark: qualityBenchmark,
        ),
        BenchmarkSection(
          benchName: "ENCODER 9A BENCHMARK",
          benchmark: encoder9ABenchmark,
        ),
        BenchmarkSection(
          benchName: "ENCODER 9B BENCHMARK",
          benchmark: encoder9BBenchmark,
        )
      ],
    );
  }
}

class BenchmarkSection extends StatefulWidget {
  final String benchName;
  final Benchmark benchmark;
  const BenchmarkSection({
    Key? key,
    required this.benchName,
    required this.benchmark,
  }) : super(key: key);

  @override
  State<BenchmarkSection> createState() => _BenchmarkSectionState();
}

class _BenchmarkSectionState extends State<BenchmarkSection> {
  final benchmarkText = StringBuffer();
  Benchmark? benchmark;
  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          children: [
            ElevatedButton(
              onPressed: () {
                setState(() {
                  benchmarkText.clear();
                  benchmark = widget.benchmark;
                });
              },
              child: Text(widget.benchName),
            ),
            ElevatedButton(
              onPressed: () {
                setState(() {
                  benchmark = null;
                  benchmarkText.clear();
                });
              },
              child: const Text("CLEAR"),
            ),
          ],
        ),
        BanchmarkLogger(
          benchmark: benchmark,
          benchmarkLogsBuffer: benchmarkText,
        ),
      ],
    );
  }
}
