import 'package:benchmark_sample/benchmarks.dart';
import 'package:flutter/material.dart';
import 'package:rxdart/rxdart.dart';
import 'isolate_functions.dart';

class BanchmarkLogger extends StatelessWidget {
  const BanchmarkLogger(
      {Key? key, this.benchmark, required this.benchmarkLogsBuffer})
      : super(key: key);

  final Benchmark? benchmark;
  final StringBuffer benchmarkLogsBuffer;

  @override
  Widget build(BuildContext context) {
    return StreamBuilder<String>(
      stream: benchmark == null
          ? null
          : launchBenchmarkInNewIsolate(benchmark!)
              .interval(const Duration(milliseconds: 160)),
      builder: (context, snapshot) {
        if (snapshot.connectionState != ConnectionState.none &&
            snapshot.hasData) {
          benchmarkLogsBuffer.writeln(snapshot.data);
        }
        return Padding(
          padding: const EdgeInsets.all(8.0),
          child: SizedBox(
            width: double.infinity,
            child: Text(benchmarkLogsBuffer.toString()),
          ),
        );
      },
    );
  }
}
