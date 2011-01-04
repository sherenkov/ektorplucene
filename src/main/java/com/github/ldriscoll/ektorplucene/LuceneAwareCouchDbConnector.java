package com.github.ldriscoll.ektorplucene;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpResponse;
import org.ektorp.http.ResponseCallback;
import org.ektorp.http.RestTemplate;
import org.ektorp.http.StdResponseHandler;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.util.Assert;
import org.joda.time.DateTime;

/**
 * Created by IntelliJ IDEA.
 * User: ldriscoll
 * Date: 12/9/10
 * Time: 2:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class LuceneAwareCouchDbConnector extends StdCouchDbConnector {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public LuceneAwareCouchDbConnector(String databaseName, CouchDbInstance dbInstance) {
        this(databaseName, dbInstance, new ObjectMapper());
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);

        CustomSerializerFactory sf = new CustomSerializerFactory();
        sf.addSpecificMapping(DateTime.class, new DateTimeSerializer());
        objectMapper.setSerializerFactory(sf);

    }

    public LuceneAwareCouchDbConnector(String databaseName, CouchDbInstance dbi, ObjectMapper om) {
        super(databaseName, dbi, om);
        this.objectMapper = om;
        this.restTemplate = new RestTemplate(dbi.getConnection());

    }

    public LuceneResult queryLucene(LuceneQuery query) {
        Assert.notNull(query, "query cannot be null");
        query.setDbPath(this.path());
        ResponseCallback<LuceneResult> rh = new StdResponseHandler<LuceneResult>() {

            public LuceneResult success(HttpResponse hr) throws Exception {
                return objectMapper.readValue(hr.getContent(), LuceneResult.class);
            }

        };
        return restTemplate.get(query.buildQuery(), rh);
    }
}