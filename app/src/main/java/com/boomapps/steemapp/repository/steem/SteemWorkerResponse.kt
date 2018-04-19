package com.boomapps.steemapp.repository.steem

/**
 * Created by Vitali Grechikha on 18.02.2018.
 */
data class SteemWorkerResponse(val result: Boolean, val errorCode: SteemErrorCodes, val errorMessage : String?) {
}