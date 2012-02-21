package models

import play.api.libs.json._


case class SocialUser(
id:Long,		
username:String,
email:String,
name:String,
first_name:String,
last_name:String,
gender:String,
birthday:String,
link:String
)


object SocialUsers {

}

