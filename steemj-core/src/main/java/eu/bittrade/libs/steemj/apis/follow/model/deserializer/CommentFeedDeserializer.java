package eu.bittrade.libs.steemj.apis.follow.model.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

import eu.bittrade.libs.steemj.apis.follow.model.CommentFeedEntry;

public class CommentFeedDeserializer  extends JsonDeserializer<CommentFeedEntry> {



    @Override
    public CommentFeedEntry deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return null;
    }
}
