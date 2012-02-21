package controllers

import play.api._
import play.api.mvc._
import play.api.libs.ws.WS
import play.api.libs.json._


import java.util._

import org.scribe.builder._
import org.scribe.builder.api._
import org.scribe.model._
import org.scribe.oauth._

import models._



object Application extends Controller {
	
  def index = Action {
	  Ok(views.html.index("Your new application is ready."))
  }
  
  def test = Action {
  	Ok("")
  }
  
}