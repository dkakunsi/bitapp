import 'package:bitapp/features/app/extension/navigation_extension.dart';
import 'package:flutter/material.dart';

class ProfilePicture extends StatelessWidget {
  final ImageProvider userImage;
  final String userSettingsRouteName;

  const ProfilePicture({
    super.key,
    required this.userImage,
    required this.userSettingsRouteName,
  });

  @override
  Widget build(BuildContext context) => Column(
    mainAxisAlignment: MainAxisAlignment.end,
    children: <Widget>[
      InkWell(
        onTap: () => context.nextRoute(userSettingsRouteName),
        child: CircleAvatar(radius: 20, backgroundImage: userImage),
      ),
    ],
  );
}
