import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import '../config/api_config.dart';
import '../theme/app_theme.dart';
import '../widgets/glass_morphic_container.dart';

class CommandConsoleScreen extends StatefulWidget {
  final Map<String, dynamic> device;

  const CommandConsoleScreen({Key? key, required this.device})
      : super(key: key);

  @override
  State<CommandConsoleScreen> createState() => _CommandConsoleScreenState();
}

class _CommandConsoleScreenState extends State<CommandConsoleScreen> {
  final TextEditingController _commandController = TextEditingController();
  bool _isExecuting = false;
  String _statusMessage = 'Ready';
  String _lastOutput = '';
  List<Map<String, dynamic>> _history = [];

  @override
  void dispose() {
    _commandController.dispose();
    super.dispose();
  }

  Future<void> _sendCommand() async {
    final command = _commandController.text.trim();
    if (command.isEmpty) return;

    final deviceId = widget.device['device_id'] as String? ?? '';
    if (deviceId.isEmpty) {
      setState(() {
        _statusMessage = 'Error: Device ID not found';
      });
      return;
    }

    setState(() {
      _isExecuting = true;
      _statusMessage = 'Sending command...';
      _lastOutput = '';
    });

    try {
      final url =
          '${ApiConfig.firestoreBase}/${ApiConfig.firebaseProjectId}/databases/(default)/documents/Devices/$deviceId/Commands?key=${ApiConfig.firebaseApiKey}';

      final document = {
        'fields': {
          'command': {'stringValue': command},
          'executed': {'booleanValue': false},
          'status': {'stringValue': 'pending'},
          'output': {'stringValue': ''},
          'return_code': {'integerValue': '0'},
          'success': {'booleanValue': false},
        }
      };

      print('Sending command to: $url');
      print('Device ID: $deviceId');

      final response = await http
          .post(
        Uri.parse(url),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode(document),
      )
          .timeout(const Duration(seconds: 15));

      print('Response status: ${response.statusCode}');
      print('Response body: ${response.body}');

      if (response.statusCode == 200 || response.statusCode == 201) {
        final data = jsonDecode(response.body);
        final name = data['name'] as String? ?? '';

        setState(() {
          _statusMessage = 'Command sent. Waiting for result...';
        });

        final result = await _pollForResult(name);

        setState(() {
          _isExecuting = false;
          _statusMessage = result['status'] as String? ?? 'Completed';
          _lastOutput = result['output'] as String? ?? '';
          _history.insert(0, {
            'command': command,
            'status': result['status'] ?? 'Completed',
            'output': result['output'] ?? '',
            'success': result['success'] ?? false,
            'return_code': result['return_code'] ?? '0',
          });
          _commandController.clear();
        });
      } else {
        setState(() {
          _isExecuting = false;
          _statusMessage =
          'Failed: HTTP ${response.statusCode} - ${response.body}';
        });
      }
    } catch (e) {
      print('Error sending command: $e');
      setState(() {
        _isExecuting = false;
        _statusMessage = 'Error: ${e.toString()}';
      });
    }
  }

  Future<Map<String, dynamic>> _pollForResult(String name) async {
    if (name.isEmpty) {
      return {
        'status': 'Failed to load result',
        'output': '',
        'success': false,
        'return_code': '0',
      };
    }

    final url =
        'https://firestore.googleapis.com/v1/$name?key=${ApiConfig.firebaseApiKey}';

    for (int i = 0; i < 20; i++) {
      final response = await http.get(Uri.parse(url));
      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        final fields = data['fields'] as Map<String, dynamic>? ?? {};

        final executed = fields['executed']?['booleanValue'] == true;
        final status = fields['status']?['stringValue'] ?? 'pending';
        final output = fields['output']?['stringValue'] ?? '';
        final success = fields['success']?['booleanValue'] == true;
        final returnCode = fields['return_code']?['integerValue'] ?? '0';

        if (executed) {
          return {
            'status': status,
            'output': output,
            'success': success,
            'return_code': returnCode,
          };
        }
      }

      await Future.delayed(const Duration(seconds: 1));
    }

    return {
      'status': 'Timed out waiting for result',
      'output': '',
      'success': false,
      'return_code': '0',
    };
  }

  @override
  Widget build(BuildContext context) {
    final deviceName = widget.device['device_name'] as String? ?? 'Device';

    return Scaffold(
      body: Container(
        color: AppTheme.primaryDark,
        child: SafeArea(
          child: Column(
            children: [
              Padding(
                padding: const EdgeInsets.all(20),
                child: Row(
                  children: [
                    GestureDetector(
                      onTap: () => Navigator.pop(context),
                      child: const Icon(
                        Icons.arrow_back,
                        color: AppTheme.textPrimary,
                      ),
                    ),
                    const SizedBox(width: 12),
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Command Console',
                          style: AppTheme.headline1(),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          deviceName,
                          style: AppTheme.bodyText2(
                            color: AppTheme.textSecondary,
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
              Expanded(
                child: ListView(
                  padding: const EdgeInsets.fromLTRB(20, 0, 20, 20),
                  children: [
                    GlassMorphicContainer(
                      padding: const EdgeInsets.all(16),
                      glassColor: AppTheme.glassLight,
                      borderRadius: BorderRadius.circular(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Execute Command',
                            style: AppTheme.bodyText1(),
                          ),
                          const SizedBox(height: 12),
                          TextField(
                            controller: _commandController,
                            style: AppTheme.bodyText1(),
                            decoration: InputDecoration(
                              hintText: 'Enter command (e.g., ipconfig)',
                              hintStyle: AppTheme.bodyText2(
                                color: AppTheme.textSecondary,
                              ),
                              filled: true,
                              fillColor:
                              AppTheme.glassTintAlt.withValues(alpha: 0.15),
                              border: OutlineInputBorder(
                                borderRadius: BorderRadius.circular(12),
                                borderSide: BorderSide.none,
                              ),
                            ),
                          ),
                          const SizedBox(height: 12),
                          SizedBox(
                            width: double.infinity,
                            child: ElevatedButton(
                              onPressed: _isExecuting ? null : _sendCommand,
                              style: ElevatedButton.styleFrom(
                                backgroundColor:
                                AppTheme.glassTint.withValues(alpha: 0.3),
                                foregroundColor: AppTheme.textPrimary,
                                elevation: 0,
                                padding:
                                const EdgeInsets.symmetric(vertical: 12),
                                shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(12),
                                ),
                              ),
                              child: Text(
                                _isExecuting ? 'Executing...' : 'Run Command',
                                style: AppTheme.bodyText1(),
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(height: 12),
                    GlassMorphicContainer(
                      padding: const EdgeInsets.all(16),
                      glassColor: AppTheme.glassLight,
                      borderRadius: BorderRadius.circular(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Status',
                            style: AppTheme.bodyText1(),
                          ),
                          const SizedBox(height: 8),
                          Text(
                            _statusMessage,
                            style: AppTheme.bodyText2(
                              color: AppTheme.textSecondary,
                            ),
                          ),
                          if (_lastOutput.isNotEmpty) ...[
                            const SizedBox(height: 12),
                            Text(
                              'Output',
                              style: AppTheme.bodyText1(),
                            ),
                            const SizedBox(height: 8),
                            Container(
                              width: double.infinity,
                              padding: const EdgeInsets.all(12),
                              decoration: BoxDecoration(
                                borderRadius: BorderRadius.circular(12),
                                color: AppTheme.glassTintAlt
                                    .withValues(alpha: 0.15),
                              ),
                              child: Text(
                                _lastOutput,
                                style: AppTheme.bodyText2(),
                              ),
                            ),
                          ],
                        ],
                      ),
                    ),
                    const SizedBox(height: 16),
                    if (_history.isNotEmpty) ...[
                      Text(
                        'Recent Commands',
                        style: AppTheme.bodyText1(),
                      ),
                      const SizedBox(height: 8),
                      for (final item in _history)
                        Padding(
                          padding: const EdgeInsets.only(bottom: 8),
                          child: GlassMorphicContainer(
                            padding: const EdgeInsets.all(14),
                            glassColor: AppTheme.glassLight,
                            borderRadius: BorderRadius.circular(12),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  item['command'] ?? '',
                                  style: AppTheme.bodyText1(),
                                ),
                                const SizedBox(height: 6),
                                Text(
                                  'Status: ${item['status']}',
                                  style: AppTheme.bodyText2(
                                    color: AppTheme.textSecondary,
                                  ),
                                ),
                                if ((item['output'] ?? '')
                                    .toString()
                                    .isNotEmpty)
                                  Padding(
                                    padding: const EdgeInsets.only(top: 6),
                                    child: Text(
                                      item['output'] ?? '',
                                      style: AppTheme.bodyText2(),
                                      maxLines: 6,
                                      overflow: TextOverflow.ellipsis,
                                    ),
                                  ),
                              ],
                            ),
                          ),
                        ),
                    ],
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
