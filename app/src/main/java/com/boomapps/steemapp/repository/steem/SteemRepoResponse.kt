/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.repository.steem

/**
 * Created by Vitali Grechikha on 18.02.2018.
 */
data class SteemRepoResponse(val result: Boolean, val errorCode: SteemErrorCodes, val errorMessage : String?) {
}