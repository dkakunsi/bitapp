import 'package:firebase_auth/firebase_auth.dart';
import 'package:google_sign_in/google_sign_in.dart';

class GoogleAuthenticationApi {
  final FirebaseAuth _firebaseAuth;
  final GoogleSignIn _googleSignIn;

  GoogleAuthenticationApi({
    FirebaseAuth? firebaseAuth,
    GoogleSignIn? googleSignIn,
  }) : _firebaseAuth = firebaseAuth ?? FirebaseAuth.instance,
       _googleSignIn = googleSignIn ?? GoogleSignIn();

  Future<common.Session> login() async {
    final googleAccount = await _googleSignIn.signIn();
    if (googleAccount == null) {
      throw Exception('Cannot get google user');
    }
    final user = await _loginToFirebase(googleAccount);
    final idToken = (await googleAccount.authentication).idToken;
    return _sessionFromFirebaseUser(user, idToken!);
  }

  Future<common.Session> silentLogin() async {
    final googleAccount = await _googleSignIn.signInSilently();
    if (googleAccount == null) {
      throw Exception('Cannot get google user');
    }
    final user = await _loginToFirebase(googleAccount);
    final token = (await googleAccount.authentication).idToken;
    return _sessionFromFirebaseUser(user, token!);
  }

  Future<User> _loginToFirebase(GoogleSignInAccount googleAccount) async {
    final googleAuth = await googleAccount.authentication;
    final idToken = googleAuth.idToken;
    if (idToken == null) {
      throw Exception('Cannot get google token');
    }
    final credential = GoogleAuthProvider.credential(
      accessToken: googleAuth.accessToken,
      idToken: googleAuth.idToken,
    );
    final user = (await _firebaseAuth.signInWithCredential(credential)).user;
    if (user == null) {
      throw Exception('Cannot get firebase user');
    }
    return user;
  }

  common.Session _sessionFromFirebaseUser(User user, String token) {
    final displayName = user.displayName;
    final email = user.email;
    if (displayName == null || email == null) {
      throw Exception('User is missing required fields');
    }
    return common.Session(
      token: token,
      userUID: user.uid,
      name: displayName,
      email: email,
      photoUrl: user.photoURL,
      phone: user.phoneNumber,
    );
  }

  Future<void> logout() async {
    await _firebaseAuth.signOut();
    await _googleSignIn.signOut();
  }
}
