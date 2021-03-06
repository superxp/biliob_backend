package com.jannchie.biliob.credit;

import com.jannchie.biliob.constant.CreditConstant;
import com.jannchie.biliob.constant.ResultEnum;
import com.jannchie.biliob.model.User;
import com.jannchie.biliob.utils.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * 此为爬虫调度器的切片。
 * 功能为防止同一爬虫调度任务多次执行。
 *
 * @author Pan Jianqi
 */
@Aspect
@Component
public class CreditOperateAspect {
    private static final Logger logger = LogManager.getLogger();
    private ArrayList<String> executing = new ArrayList<>();

    public CreditOperateAspect() {
    }

    @Pointcut(value = "execution(public * com.jannchie.biliob.credit.handle.CreditOperateHandle.*(com.jannchie.biliob.model.User,com.jannchie.biliob.constant.CreditConstant,..))")
    public void checkCredit() {
    }

    @Pointcut(value = "execution(public * com.jannchie.biliob.credit.handle.CreditOperateHandle.doCustomCreditOperate(com.jannchie.biliob.model.User,..))")
    public void checkCreditGuessing() {
    }

    @Around(value = "checkCreditGuessing() && args(user, credit, ..)", argNames = "user,credit")
    public Object doAround(ProceedingJoinPoint pjp, User user, Double credit) throws Throwable {
        if (user == null) {
            return new Result<>(ResultEnum.HAS_NOT_LOGGED_IN);
        } else if (credit > 0 && user.getCredit() < (credit)) {
            logger.info("用户：{},积分不足,当前积分：{}", user.getName(), user.getCredit());
            return new Result<>(ResultEnum.CREDIT_NOT_ENOUGH);
        }
        return pjp.proceed();
    }

    @Around(value = "checkCredit() && args(user, creditConstant, ..)", argNames = "user,creditConstant")
    public Object doAround(ProceedingJoinPoint pjp, User user, CreditConstant creditConstant) throws Throwable {
        Double value = creditConstant.getValue();
        if (user == null) {
            return new Result<>(ResultEnum.HAS_NOT_LOGGED_IN);
        } else if (value < 0 && user.getCredit() < (-value)) {
            logger.info("用户：{},积分不足,当前积分：{}", user.getName(), user.getCredit());
            return new Result<>(ResultEnum.CREDIT_NOT_ENOUGH);
        }
        return pjp.proceed();
    }
}