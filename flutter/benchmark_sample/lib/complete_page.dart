import 'package:benchmark_sample/benchmarks.dart';
import 'package:flutter/material.dart';

import 'benchmark_logger.dart';

class CompletePage extends StatefulWidget {
  const CompletePage({Key? key}) : super(key: key);

  @override
  State<CompletePage> createState() => _CompletePageState();
}

class _CompletePageState extends State<CompletePage> {
  final benchmarkText = StringBuffer();
  @override
  Widget build(BuildContext context) {
    return BanchmarkLogger(
      benchmark: completeBenchmark,
      benchmarkLogsBuffer: benchmarkText,
    );
  }
}
