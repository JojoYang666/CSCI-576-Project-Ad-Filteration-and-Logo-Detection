public class YUV{
    private double[][] arrayY;
    private double[][] arrayU;  // Pb is put here
    private double[][] arrayV;  // Pr is put here

    public YUV(double[][] y, double[][] u, double[][] v){
        this.arrayY = y;
        this.arrayU = u;
        this.arrayV = v;
    }
  
    public double[][] getY(){
        return(arrayY);
    }
  
    public double[][] getU(){
        return(arrayU);
    }
  
    public double[][] getV(){
        return(arrayV);
    }
}