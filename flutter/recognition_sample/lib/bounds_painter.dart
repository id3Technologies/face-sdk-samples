import 'package:flutter/material.dart';

class BoundsPainter extends CustomPainter {
  final Rect bounds;
  final int imageWidth;
  final int imageHeight;

  BoundsPainter({
    required this.bounds,
    required this.imageWidth,
    required this.imageHeight,
  });

  @override
  void paint(Canvas canvas, Size size) {
    if (bounds != Rect.zero) {
      final scaledRect = _scaleRectangle(bounds, size);

      canvas.drawRect(
        scaledRect,
        Paint()
          ..color = Colors.green
          ..strokeWidth = 3
          ..style = PaintingStyle.stroke,
      );
    }
  }

  @override
  bool shouldRepaint(covariant BoundsPainter oldDelegate) {
    return bounds != oldDelegate.bounds;
  }

  Rect _scaleRectangle(Rect rect, Size size) {
    return Rect.fromLTRB(
      size.width - rect.left * size.width / imageWidth,
      rect.top * size.height / imageHeight,
      size.width - rect.right * size.width / imageWidth,
      rect.bottom * size.height / imageHeight,
    );
  }
}
