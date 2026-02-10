import 'package:app_common/app_common.dart';
import 'package:flutter/material.dart';

class UserInfo extends StatelessWidget {
  final UserViewModel user;
  final String logoutLabel;

  UserInfo({super.key, required this.user, required this.logoutLabel});

  @override
  Widget build(BuildContext context) {
    return BoxContainer(
      child: Column(
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.start,
            children: <Widget>[
              CircleAvatar(radius: 40, backgroundImage: context.userImage),
              SizedBox(width: 24),
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  Text(
                    user.name,
                    style: TextStyles.appMain(
                      fontSize: AppFontSize.large,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  SizedBox(height: 2),
                  Text(
                    user.email,
                    style: TextStyles.appDetail(fontSize: AppFontSize.small),
                  ),
                  SizedBox(height: 2),
                  Text(
                    user.phone,
                    style: TextStyles.appDetail(fontSize: AppFontSize.small),
                  ),
                ],
              ),
            ],
          ),
          SizedBox(height: 12),
          LogoutButton(logoutLabel: logoutLabel),
        ],
      ),
    );
  }
}

class LogoutButton extends StatelessWidget {
  final String logoutLabel;

  const LogoutButton({super.key, required this.logoutLabel});

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.start,
      children: <Widget>[
        InkWell(
          onTap: () {
            context.logout();
          },
          child: Container(
            width: 80,
            alignment: Alignment.center,
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: AppColor.green,
              borderRadius: BorderRadius.circular(12),
            ),
            child: Text(
              logoutLabel,
              style: TextStyles.appDetail(
                fontColor: AppColor.white,
                fontSize: AppFontSize.small,
              ),
            ),
          ),
        ),
      ],
    );
  }
}
