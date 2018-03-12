package com.boomapps.steemapp.repository.entity

import com.google.gson.annotations.SerializedName

/**
 * Created by Vitali Grechikha on 28.01.2018.
 */
class UserDataEntity {

    // metadata={"profile":{"profile_image":"https://scontent.fhen1-1.fna.fbcdn.net/v/t1.0-9/14691122_10154463397915609_2082685872488296188_n.jpg?oh=69fb67724f817bde4417463fcb8fce00&oe=59887F42","name":"Yuriy","about":"A family guy, a father, a husband, a crypto fan, especially Steemit, a dog and cat person, love to travel, develop websites, ski and much more.","location":"Planet Earth","website":"https://golos.io/@yuriks2000","cover_image":"https://images.golos.io/DQmWXvCMpQz4Y3GJVGwJK1AhY7oWPrrc2uDRxfAX83h1kai/maldives-2122547_1920.jpg"}}

    @SerializedName("profile_image")
    var photoUrl: String = ""

    @SerializedName("name")
    var userName: String = ""

    @SerializedName("about")
    var userAbout: String = ""

    @SerializedName("location")
    var location: String = ""

    @SerializedName("website")
    var website: String = ""

    @SerializedName("cover_image")
    var cover_image: String = ""

}