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

	//ע��Service����
	@Autowired
	private SeckillDao seckillDao;

	@Autowired
	private SuccessKilledDao successKilledDao;

	// md5��ֵ�ַ����������޷��µ�����
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
		// ϵͳ��ǰʱ��
		Date nowTime = new Date();
		if (nowTime.getTime() < startTime.getTime() || nowTime.getTime() > endTime.getTime()) {
			return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
		}
		// ת���ض��ַ����Ĺ���
		String md5 = getMD5(seckillId);
		return new Exposer(true, md5, seckillId);
	}

	private String getMD5(long seckillId) {
		String base = seckillId + "/" + strSlat;
		return DigestUtils.md5DigestAsHex(base.getBytes());
	}

	@Transactional
	/**
	 * ʹ��ע��������񷽷� ���е�
	 * 1�������ŶӴ��һ��Լ������ȷ��ע���񷽷� �ı�̷��
	 * 2����֤���񷽷���ִ��ʱ�価���̣ܶ���Ҫ���������������RPC��HHTP������߰��뵽���񷽷��ⲿ
	 * 3���������еķ�����Ҫ������ֻ��һ���޸Ĳ���
	 */
	public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5){
		if (md5 == null || md5.equals(getMD5(seckillId))) {
			throw new SeckillException("seckill data rewrite");
		}
		// ִ����ɱ�߼��������+��¼������Ϊ
		Date nowTime = new Date();
		try {
			int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
			if (updateCount <= 0) {
				// û�и��µ���¼
				throw new SeckillCloseException("seckill is closed");
			} else {
				// ��¼������Ϊ
				int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
				// Ψһ��seckillId,userPhone
				if (insertCount <= 0) {
					//�ظ���ɱ
					throw new RepeatKillException("seckill repeated");
				} else {
					// ��ɱ�ɹ�
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
			//���б������쳣��ת��Ϊ�������쳣
			throw new SeckillException("seckill inner error:"+e.getMessage());
		}
	}

}
