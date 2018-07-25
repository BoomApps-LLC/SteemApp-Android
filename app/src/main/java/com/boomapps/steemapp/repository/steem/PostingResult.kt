package com.boomapps.steemapp.repository.steem

data class PostingResult(var code : SteemAnswerCodes, var result: String = "Posting was successful", var success: Boolean = true)