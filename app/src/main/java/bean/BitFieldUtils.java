package bean;

public class BitFieldUtils {

    //将字符串转换为字节数组,第一个字符转换为字节取后四位和第二个字符转换为字节后取后四位组成一个新的字节
    public static byte[] biteFieldToByteArray(String src) {
        if (src.isEmpty()) {
            return null;
        }
        char[] chars = src.toCharArray();
       // System.out.println("length==="+chars.length);
        int len = chars.length / 2 + chars.length % 2;
       // System.out.println("len==="+len);
        byte[] result = new byte[len];
        byte resultLen=0;
        for (byte i = 0; i < chars.length; i +=2) {
           // System.out.println("i==="+i);
            if(chars.length%2!=0&&i==chars.length-1){
                result[resultLen] = bitFieldToByte((byte) Integer.parseInt(chars[i] + ""), (byte)0);
            }else {
                result[resultLen] = bitFieldToByte((byte) Integer.parseInt(chars[i] + ""), (byte) Integer.parseInt(chars[i + 1] + ""));
            }
            resultLen++;
        }
        return result;
    }

    //取a字节后四位和b字节的后四位组成一个新的字节,a字节的后四位放在高位,b字节的后四位放在低位
    public static byte bitFieldToByte(byte a, byte b) {
        return (byte) (a << 4 | b);
    }
}
