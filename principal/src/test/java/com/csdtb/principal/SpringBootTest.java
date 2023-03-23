package com.csdtb.principal;

/**
 * @author zhoujiacheng
 * @date 2023-03-17
 */
public class SpringBootTest {

    public static void main(String args[]){
        A a = new A();
        a.setA(1);
        a.setB(2);
        B b = a;
        System.out.println(b);
        System.out.println(a);
    }

    static class A extends B{
        private int a;

        public int getA() {
            return a;
        }

        public void setA(int a) {
            this.a = a;
        }

        @Override
        public String toString() {
            return "A{" +
                    "a=" + a + "b=" + getB() +
                    '}';
        }
    }

    static class B{
        private int b;

        public int getB() {
            return b;
        }

        public void setB(int b) {
            this.b = b;
        }

        @Override
        public String toString() {
            return "B{" +
                    "b=" + b +
                    '}';
        }
    }
}
