package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.Logger

import java.util._

import org.scribe.builder._
import org.scribe.builder.api._
import org.scribe.model._
import org.scribe.oauth._

import play.api.Configuration._

import models._



object FacebookController extends Controller {
	lazy val config = Configuration.load()
	val FBAppId = config.getString("fb.app.id").getOrElse("")
	val FBAppSecret = config.getString("fb.app.secretkey").getOrElse("")
	val FBAppCallback = config.getString("fb.app.callback").getOrElse("http://www.testdomain.in/fbLogin/code")
	val NETWORK_NAME = "Facebook"
	val PROTECTED_RESOURCE_URL = "https://graph.facebook.com/me"
	var EMPTY_TOKEN:Token = null;

	val service:OAuthService  = new ServiceBuilder()
                                  .provider(classOf[FacebookApi])
                                  .apiKey(FBAppId)
                                  .apiSecret(FBAppSecret)
                                  .callback(FBAppCallback)
                                  .build();
	

	def fbConnect = Action {
		 Logger.debug("Fetching the Authorization URL...");
		 val authorizationUrl = service.getAuthorizationUrl(EMPTY_TOKEN);
		 Logger.debug("Got the Authorization URL!:" + authorizationUrl);
		 Logger.debug("Now redirecting to the URL for authorization.......");
		 Redirect(authorizationUrl)		
	}

	def fbLoginCode = Action{implicit request =>
	Logger.info("Parsing returned code from fb...")
	try {
    request.queryString.get("code") match {
                                case Some(code) =>
                                		Logger.debug("Code received from db:" + code(0))
                                	    var verifier:Verifier = new Verifier(code(0));
    
                                		var accessToken:Token = service.getAccessToken(EMPTY_TOKEN, verifier);
                                		Logger.debug("Got the Access Token!");
                                		Logger.debug("Access token is: " + accessToken + " )");

                                		Logger.debug("Fetch the user information now...");
                                		var request:OAuthRequest = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
                                		service.signRequest(accessToken, request);
                                		var response:org.scribe.model.Response = request.send();
                                		Logger.debug("Code received is:"  + response.getCode());
						val body = response.getBody()
                                		Logger.debug("Response data received is:" + body);
                                		Logger.debug("Starting parsing the JSON response as SocialUser object")
						val user:SocialUser = getSocialUser(body)
						Ok("User email is:" + user.email + " and username is:" + user.username)
                                case _ =>
                                	Logger.debug("I should not be here => Code received is invalid or empty")
					BadRequest("Unknown Error")
    }
	}
	catch {
		case exp:Exception =>
			Logger.error("Sorry, an error has occurred. Message from server is: " + exp.getMessage())
			exp.printStackTrace()
		BadRequest(exp.getMessage)
	}

}

  implicit object UserFormat extends Format[SocialUser] {
    def reads(json: JsValue): SocialUser = SocialUser(id=
      (json \ "id").as[String].toLong,
      name=(json \ "name").as[String],
      first_name=(json \ "first_name").as[String],
      last_name=(json \ "last_name").as[String],
      link=(json \ "link").as[String],
      username=(json \ "username").as[String],
      birthday=(json \ "birthday").as[String],
      email=(json \ "email").as[String],
      gender=(json \ "gender").as[String]
    )
    def writes(u: SocialUser): JsValue = JsObject(Seq(
        "id" -> JsNumber(u.id),
        "name" -> JsString(u.name),
        	"first_name" -> JsString(u.first_name),
                "last_name" -> JsString(u.last_name),
                        "link" -> JsString(u.link),
                                "username" -> JsString(u.username),
                                        "birthday" -> JsString(u.birthday),
                                                "email" -> JsString(u.email),
                                                	"gender" -> JsString(u.gender)))  
  }		
	
  private def getSocialUser(fbuser:String):SocialUser = play.api.libs.json.Json.parse(fbuser).as[SocialUser]
  
  def test = Action {
  	Ok("")
  }
  
}
