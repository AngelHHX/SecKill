package Service;

import java.util.List;

import Dto.Exposer;
import Dto.SeckillExecution;
import Entity.Seckill;
import Exception.RepeatKillException;
import Exception.SeckillCloseException;
import Exception.SeckillException;

/**
 * 业务接口：站在“使用者”角度设计接口
 * @author AngelHHX
 *
 */
public interface SeckillService {

	/**
	 * 查询所有秒杀记录
	 * @return
	 */
	List<Seckill> getSeckillList();
	
	/**
	 * 查询单个秒杀记录
	 * @param seckillId
	 * @return
	 */
	Seckill getById(long seckillId);
	
	/**
	 * 秒杀开启是输出秒杀接口地址
	 * 否则输出系统时间和秒杀时间
	 * @param seckillId
	 */
	Exposer exportSeckillUrl(long seckillId);
	
	/**
	 * 执行秒杀操作
	 * @param seckillId
	 * @param userPhone
	 * @param md5
	 * @return
	 */
	SeckillExecution executeSeckill(long seckillId,long userPhone,String md5)
		throws SeckillException,RepeatKillException,SeckillCloseException;
}
