import 'dart:async';

import 'package:camera/camera.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';

import 'analyze_face_result.dart';
import 'analyze_process.dart';
import 'bounds_painter.dart';
import 'capture_process.dart';

class AnalyzePage extends StatefulWidget {
  const AnalyzePage({Key? key}) : super(key: key);

  @override
  State<AnalyzePage> createState() => _AnalyzePageState();
}

class _AnalyzePageState extends State<AnalyzePage> {
  Future<AnalyzeFaceResult>? analyzeResultFuture;
  CameraController? controller;
  CaptureProcessResult? lastResult;
  CaptureProcess? process;
  bool processingImage = false;

  @override
  void dispose() {
    controller?.dispose();
    super.dispose();
  }

  @override
  void initState() {
    super.initState();
    availableCameras().then((cameras) {
      controller = CameraController(
        cameras.firstWhere(
            (camera) => camera.lensDirection == CameraLensDirection.front),
        ResolutionPreset.max,
        enableAudio: false,
      );
      controller!.initialize().then((_) {
        if (!mounted) {
          return;
        }
        process = CaptureProcess(controller!.description.sensorOrientation);
        setState(() {});
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          SizedBox(
            height: 256,
            child: (controller == null || !controller!.value.isInitialized)
                ? Container()
                : CustomPaint(
                    foregroundPainter: BoundsPainter(
                      bounds: lastResult?.faceBounds ?? Rect.zero,
                      imageWidth: lastResult?.imageWidth ?? 0,
                      imageHeight: lastResult?.imageHeight ?? 0,
                    ),
                    child: CameraPreview(controller!),
                  ),
          ),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: <Widget>[
              ElevatedButton(
                onPressed: () async {
                  await process?.isReady;
                  if (controller?.value.isStreamingImages ?? false) {
                    await controller?.stopImageStream();
                    setState(() {
                      lastResult = null;
                    });
                  } else {
                    controller?.startImageStream((cameraImage) async {
                      if (!processingImage) {
                        processingImage = true;
                        process?.processCameraImage(cameraImage).then((result) {
                          setState(() {
                            lastResult = result;
                            processingImage = false;
                          });
                        });
                      }
                    });
                  }
                },
                child: Text(
                  (controller?.value.isStreamingImages ?? false)
                      ? 'STOP CAPTURE'
                      : 'START CAPTURE',
                ),
              ),
              ElevatedButton(
                onPressed: () async {
                  if (lastResult != null) {
                    setState(() {
                      analyzeResultFuture = compute(onAnalyze, lastResult!);
                    });
                  }
                },
                child: const Text('ANALYZE FACE'),
              ),
            ],
          ),
          const SizedBox(height: 32),
          FutureBuilder<AnalyzeFaceResult>(
            future: analyzeResultFuture,
            builder: (context, snapshot) {
              if (snapshot.connectionState == ConnectionState.waiting) {
                return const CircularProgressIndicator();
              } else if (snapshot.connectionState == ConnectionState.done) {
                final analyzeResult = snapshot.data!;
                return Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    Image.memory(
                      analyzeResult.portraitImage,
                      width: 100,
                    ),
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        if (analyzeResult.isPoseOk) ...[
                          const OkText(text: 'Correct pose'),
                          if (analyzeResult.isOcclusionOk) ...[
                            const OkText(text: 'No occlusion'),
                            if (analyzeResult.hasHat)
                              const OkText(text: 'No hat')
                            else
                              const NotOkText(text: 'Hat detected'),
                            if (analyzeResult.hasMouthOpen)
                              const OkText(text: 'Mouth closed')
                            else
                              const NotOkText(text: 'Mouth open detected'),
                            if (analyzeResult.hasSmile)
                              const OkText(text: 'No smile')
                            else
                              const NotOkText(text: 'Smile detected')
                          ] else
                            const NotOkText(text: 'Occlusion detected')
                        ] else
                          const NotOkText(text: 'Incorrect pose'),
                      ],
                    )
                  ],
                );
              } else {
                return const SizedBox.shrink();
              }
            },
          ),
        ],
      ),
    );
  }
}

class OkText extends StatelessWidget {
  final String? text;
  const OkText({Key? key, this.text}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return RichText(
      text: TextSpan(
        style: const TextStyle(color: Colors.black),
        children: [
          const TextSpan(
            text: "OK",
            style: TextStyle(color: Colors.green),
          ),
          if (text != null) TextSpan(text: ' : $text'),
        ],
      ),
    );
  }
}

class NotOkText extends StatelessWidget {
  final String? text;
  const NotOkText({Key? key, this.text}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return RichText(
      text: TextSpan(
        style: const TextStyle(color: Colors.black),
        children: [
          const TextSpan(
            text: "NOT OK",
            style: TextStyle(color: Colors.red),
          ),
          if (text != null) TextSpan(text: ' : $text'),
        ],
      ),
    );
  }
}
