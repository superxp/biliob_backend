package com.jannchie.biliob.utils.credit.calculator;

import com.jannchie.biliob.constant.ResultEnum;
import com.jannchie.biliob.model.Author;
import com.jannchie.biliob.object.AuthorIntervalRecord;
import com.jannchie.biliob.utils.Result;
import com.mongodb.BasicDBObject;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

import static com.jannchie.biliob.constant.TimeConstant.SECOND_OF_MINUTES;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

/**
 * @author jannchie
 */
@Component
public class ForceFocusCreditCalculator extends AbstractCreditCalculator {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public ForceFocusCreditCalculator(MongoTemplate mongoTemplate) {
        super(mongoTemplate);
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    ResponseEntity execute(Long id, ObjectId objectId) {

        BasicDBObject fieldsObject = new BasicDBObject();
        BasicDBObject dbObject = new BasicDBObject();
        dbObject.put("mid", id);
        fieldsObject.put("forceFocus", true);
        Author author =
                mongoTemplate.findOne(
                        new BasicQuery(dbObject.toJson(), fieldsObject.toJson()), Author.class);
        if (author == null) {
            mongoTemplate.remove(Query.query(Criteria.where("_id").is(objectId)), "user_record");
            return new ResponseEntity<>(new Result<>(ResultEnum.AUTHOR_NOT_FOUND), HttpStatus.ACCEPTED);
        } else if (author.getForceFocus() != null && author.getForceFocus()) {
            mongoTemplate.remove(Query.query(Criteria.where("_id").is(objectId)), "user_record");
            return new ResponseEntity<>(new Result<>(ResultEnum.ALREADY_FORCE_FOCUS), HttpStatus.ACCEPTED);
        }

        mongoTemplate.updateFirst(query(where("mid").is(id)), update("forceFocus", true), Author.class);
        super.setExecuted(objectId);
        upsertAuthorFreq(id, SECOND_OF_MINUTES * 60 * 12);
        return null;
    }

    public void upsertAuthorFreq(Long mid, Integer interval) {
        AuthorIntervalRecord preInterval =
                mongoTemplate.findOne(Query.query(Criteria.where("mid").is(mid)),
                        AuthorIntervalRecord.class, "author_interval");
        Calendar nextCal = Calendar.getInstance();
        Date cTime = Calendar.getInstance().getTime();
        nextCal.add(Calendar.SECOND, interval);
        // 更新访问频率数据。

        Update u = Update.update("date", cTime).set("interval", interval);
        // 如果此前没有访问频率数据，或者更新后的访问时间比原来的访问时间还短，则刷新下次访问的时间。
        if (preInterval == null
                || nextCal.getTimeInMillis() < preInterval.getNext().getTime()) {
            u.set("next", nextCal.getTime());
        }
        mongoTemplate.upsert(Query.query(Criteria.where("mid").is(mid)), u, "author_interval");
    }
}
