package Entity;
import java.util.Date;

public class SuccessKilled {
	private long secKillId;
	private long userPhone;
	private short state;
	private Date createTime;

	// 多对一,因为一件商品在库存中有很多数量，对应的购买明细也有很多。
	private Seckill seckill;

	public long getSecKillId() {
		return secKillId;
	}

	public void setSecKillId(long secKillId) {
		this.secKillId = secKillId;
	}

	public long getUserPhone() {
		return userPhone;
	}

	public void setUserPhone(long userPhone) {
		this.userPhone = userPhone;
	}

	public short getState() {
		return state;
	}

	public void setState(short state) {
		this.state = state;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Seckill getSeckill() {
		return seckill;
	}

	public void setSeckill(Seckill seckill) {
		this.seckill = seckill;
	}

	@Override
	public String toString() {
		return "SuccessKilled{" + "secKillId=" + secKillId + ", userPhone=" + userPhone + ", state=" + state + ", createTime=" + createTime + '}';
	}
}
