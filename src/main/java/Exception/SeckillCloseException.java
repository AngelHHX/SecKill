package Exception;

/**
 * ��ɱ�ر��쳣
 * @author AngelHHX
 *
 */
public class SeckillCloseException extends SeckillException{

	public SeckillCloseException(String message){
		super(message);
	}
	
	public SeckillCloseException(String message,Throwable cause){
		super(message,cause);
	}
	
}
