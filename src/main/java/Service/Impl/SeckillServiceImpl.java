package Service.Impl;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import Dao.SeckillDao;
import Dao.SuccessKilledDao;
import Dto.Exposer;
import Dto.SeckillExecution;
import Entity.Seckill;
import Entity.SuccessKilled;
import Enums.SeckillStatEnum;
import Exception.RepeatKillException;
import Exception.SeckillCloseException;
import Exception.SeckillException;
import Service.SeckillService;

@Service
public class SeckillServiceImpl implements SeckillService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	//注入Service依赖
	@Autowired
	private SeckillDao seckillDao;

	@Autowired
	private SuccessKilledDao successKilledDao;

	// md5掩值字符串，让人无法猜到密码
	private static final String strSlat = "sdklg89023&*0@3423#$%&*@sdfad7890";

	public List<Seckill> getSeckillList() {
		return seckillDao.queryAll(0, 4);
	}

	public Seckill getById(long seckillId) {
		return seckillDao.queryById(seckillId);
	}

	public Exposer exportSeckillUrl(long seckillId) {
		Seckill seckill = seckillDao.queryById(seckillId);
		if (seckill == null) {
			return new Exposer(false, seckillId);
		}

		Date startTime = seckill.getStartTime();
		Date endTime = seckill.getEndTime();
		// 系统当前时间
		Date nowTime = new Date();
		if (nowTime.getTime() < startTime.getTime() || nowTime.getTime() > endTime.getTime()) {
			return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
		}
		// 转化特定字符串的过程
		String md5 = getMD5(seckillId);
		return new Exposer(true, md5, seckillId);
	}

	private String getMD5(long seckillId) {
		String base = seckillId + "/" + strSlat;
		return DigestUtils.md5DigestAsHex(base.getBytes());
	}

	@Transactional
	/**
	 * 使用注解控制事务方法 的有点
	 * 1，开发团队达成一致约定，明确标注事务方法 的编程风格
	 * 2，保证事务方法的执行时间尽可能短，不要穿插其他网络操作RPC、HHTP请求或者剥离到事务方法外部
	 * 3，不是所有的方法都要事务，如只有一条修改操作
	 */
	public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5){
		if (md5 == null || md5.equals(getMD5(seckillId))) {
			throw new SeckillException("seckill data rewrite");
		}
		// 执行秒杀逻辑：减库存+纪录购买行为
		Date nowTime = new Date();
		try {
			int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
			if (updateCount <= 0) {
				// 没有更新到记录
				throw new SeckillCloseException("seckill is closed");
			} else {
				// 记录购买行为
				int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
				// 唯一：seckillId,userPhone
				if (insertCount <= 0) {
					//重复秒杀
					throw new RepeatKillException("seckill repeated");
				} else {
					// 秒杀成功
					SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
					return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
				}
			}
		}catch(SeckillCloseException e1){
			throw e1;
		} catch(RepeatKillException e2){
			throw e2;
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
			//所有编译期异常，转化为运行期异常
			throw new SeckillException("seckill inner error:"+e.getMessage());
		}
	}

}
