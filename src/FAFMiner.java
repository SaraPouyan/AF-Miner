package weka.classifiers.custom;

import net.sourceforge.jFuzzyLogic.FIS;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;

import java.util.*;

public class FAFMiner extends Classifier {
    //
    private double[][][] classifierRuleList;
    private int dim;
    private int classNum;
    private Normalize filter;
    private Random random;



    @Override
    public void buildClassifier(Instances data) throws Exception {

        long seed = 1;
        random = new Random(seed);

        //
        dim = data.numAttributes() - 1;
        classNum = data.numClasses();

        //
        filter = new Normalize();
        filter.setInputFormat(data);
        data = Filter.useFilter(data, filter);


        //
        List<double[]> dataList = new ArrayList<double[]>();
        Enumeration<Instance> e = data.enumerateInstances();
        while (e.hasMoreElements()) {
            dataList.add(e.nextElement().toDoubleArray());
        }

        //
        classifierRuleList = new double[classNum][dim][2];
        for (int i = 0; i < classNum; i++) {
            for (int j = 0; j < dim; j++) {

                //AFSA
                double[][] rule = getRule(dataList, i);

                //Pruning
                rule = prune(dataList, rule, i);
                classifierRuleList[i] = rule;

                //Cleaning
                List<double[]> dataListTemp = cleaning(dataList, rule);
                if (dataListTemp.size() != 0) {
                    dataList = dataListTemp;
                    break;
                }

            }

        }

    }


    private double[][] prune(List<double[]> dataList, double[][] rule, int classId) {
        for (int i = 0; i < dim; i++) {
            //
            double[][] ruleTemp = new double[dim][2];
            for (int j = 0; j < dim; j++) {
                ruleTemp[j][0] = rule[j][0];
                ruleTemp[j][1] = rule[j][1];
            }
            //
            ruleTemp[i][0] = Double.NaN;
            ruleTemp[i][1] = Double.NaN;

            double q1 = q(dataList, rule, classId);
            double q2 = q(dataList, ruleTemp, classId);

            if(q2 >= q1) {
                rule[i][0] = Double.NaN;
                rule[i][1] = Double.NaN;
            }
        }
        return rule;
    }

    private List<double[]> cleaning(List<double[]> dataList, double[][] rule) {
        List<double[]> dataListTemp = new ArrayList<double[]>(dataList);
        for (int j = dataListTemp.size() - 1; j >= 0; j--) {
            if (test(rule, dataListTemp.get(j))) {
                dataListTemp.remove(j);
            }
        }

        return dataListTemp;
    }

    @Override
    public double classifyInstance(Instance instance) throws Exception {
        //
        filter.input(instance);
        instance = filter.output();
        double[] d = instance.toDoubleArray();

        //
        for (int i = 0; i < classNum; i++) {
            if (test(classifierRuleList[i], d)){
                return i; //True Detect
            }
        }

        //
        return 0;
    }

    public double[][] getRule(List<double[]> dataList3, final double classId2){
        final List<double[]> dataList2 = dataList3;

        FAFSA fafsa = new FAFSA(dim * 2, random) {
            @Override
            public double fitness(double[] af) {
                double[][] rule = new double[dim][2];
                for (int i = 0; i < dim; i++) {
                    rule[i][0] = Math.min(af[i * 2], af[i * 2 + 1]);
                    rule[i][1] = Math.max(af[i * 2],af[i * 2 + 1]);
                }
                double q = q(dataList2, rule, classId2);
                double fitness = q;
                return fitness;
            }
        };
        return fafsa.run();
    }

    public double q(List<double[]> data2, double[][] rule, double classId){
        //


        double tp = 0, fp = 0, tn = 0, fn = 0, q = 0;
        int count = 0;
        int []flag = new int[dim];
        for (int l = 0; l < dim; l++){
            flag[l] = 0;
        }

        for (int m = 0; m < dim; m++) {
            if( rule[m][0] >= 0 && 1 >= rule[m][1] && rule[m][1] >= 0 && 1 >= rule[m][0] ) {
                flag[m] = 1;

                for (int i = 0; i < data2.size(); i++) {
                    double[] d = data2.get(i);


                    if (rule[m][0] <= d[m] && rule[m][1] >= d[m]) {
                        if (classId == d[dim]  )

                            tp += (double) 1/dim;


                        else

                            fp += (double) 1/dim;

                    }
                    else {
                        if (classId == d[dim])


                            fn += (double) 1/dim;
                        else

                            tn += (double) 1/dim;

                    }
                }

                double sensitivity, spesivity;
                sensitivity = tp / (tp + fn) ;
                spesivity = tn / (tn + fp);

                q =  sensitivity * spesivity;

            }

        }
        for (int h = 0; h < flag.length;h++){
            if (flag[h] == 0)
                count++;
        }
        return (double)((-1/dim)) * count + q;
    }

    public boolean test(double[][] rule, double[] data){
        for (int i = 0; i < dim; i++) {
            if(data[i] < rule[i][0] || rule[i][1] < data[i])
                return false;
        }

        return true;
    }



    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("Welcome\n");
        str.append("FAF-Miner\n");

        if(classifierRuleList == null)
            return str.toString();
        //
        str.append("Classifier Rules:\n");
        for (int i = 0; i < classifierRuleList.length; i++) {
            str.append(String.format("Rule %d: ", i + 1));
            for (int j = 0; j < classifierRuleList[i].length; j++) {
                str.append(String.format("{%.5f, %.5f}",
                        classifierRuleList[i][j][0],
                        classifierRuleList[i][j][1])
                );
            }
            str.append("\n");
        }
        str.append("----------");

        //
        str.append("Finish!\n");
        return str.toString();
    }
}


//*****************************************************************************

abstract class FAFSA {
    private double[][] population;
    private double[][] tempPopulation;
    private int populationNumber      = 20;
    public double[] fishValue         = new double[populationNumber] ;
    private double[] distancefromBest = new double[populationNumber];
    private double[] rank             = new double[populationNumber];
    private double[] fitnessRank      = new double[populationNumber];
    private double[] CW               = new double[populationNumber];
    private double[] step             = new double[populationNumber];
    private double[] visual           = new double[populationNumber];
    private double maxFitness         = Double.MIN_VALUE;
    private int tryNumber             = 10;
    private double crowdFactor        = 0.24;
    private int maxIteration          = 40;
    private int dimNumber;
    private double Itrnum;
    private double[] bestAf ;
    private double [][]pop;
    Random random;

    public FAFSA(int dim, Random random) {
        //
        dimNumber      = dim;
        population     = new double[populationNumber][dimNumber];
        tempPopulation = new double[populationNumber][dimNumber];
        bestAf         = new double[dimNumber];

        //
        this.random = random;

        //
        for (int i = 0; i < populationNumber; i++) {
            for (int j = 0; j < dimNumber / 2; j++) {
                double d1                =  random.nextDouble();
                double d2                =  random.nextDouble();
                population[i][j * 2]     = Math.min(d1, d2);
                population[i][j * 2 + 1] = Math.max(d1, d2);
            }
        }


    }

    abstract public double fitness(double[] af);

    public double getFitness(double[] af){
        double fitness = fitness(af);
        //
        if(fitness > maxFitness) {
            bestAf     = af.clone();
            maxFitness = fitness;
        }
        return fitness;
    }

    public double[][] run() {
        String fileName = "src/weka/classifiers/custom/FAF.fcl";
        FIS fis         = FIS.load(fileName, true);

        for (int i = 0; i < maxIteration; i++) {

            Itrnum = (double) i/maxIteration;
            for (int d = 0; d < populationNumber; d++){

                distancefromBest[d]   = len(population[d],bestAf);
                int[] idx             = getIndicesInOrder(distancefromBest);
                int[] index           = getIndicesInOrder(fishValue);
                rank[idx[d]]          = (double) (populationNumber-d) / populationNumber;
                fitnessRank[index[d]] = (double) d / populationNumber;

                fis.setVariable("IterationNumber", Itrnum);
                fis.setVariable("DistanceFromBest", rank[d] );
                fis.setVariable("FitnessRanking", fitnessRank[d] );
                fis.evaluate();

                CW[d]     = fis.getVariable("Weight").getValue();

                if (i == 0){
                    for (int m = 0; m < populationNumber; m++){
                        visual[m] =  0.56 ;
                        step[m]   =  0.44;
                    }
                }
                else{
                    visual[d] = CW[d] * visual[d];
                    step[d]   = CW[d] * step[d];
                }


                double[][] f = follow();
                double[][] s = swarm();

                for (int j = 0; j < populationNumber; j++) {
                    if(getFitness(s[j]) <= getFitness(f[j]))
                        population[j] = f[j];
                    else
                        population[j] = s[j];
                }
            }
        }

        double[][] d = new double[dimNumber / 2][2];
        for (int i = 0; i < dimNumber / 2; i++) {
            d[i][0] = Math.min(bestAf[i * 2],bestAf[i * 2 + 1]);
            d[i][1] = Math.max(bestAf[i * 2],bestAf[i * 2 + 1]);
        }
        return d;
    }


    public double[][] follow() {
        for (int n = 0; n < populationNumber; n++){
            fishValue[n] = getFitness(population[n]);
        }

        final int[] follow_ok = new int[populationNumber];
        for(int i = 0; i < populationNumber; i++){
            for (int j = 0; j < populationNumber; j++){
                int[] fishID = getIndicesInOrder(fishValue);
                if ( fishID[j] != i && len(population[i], population[fishID[j]]) <= visual[fishID[j]] && fishValue[fishID[j]] > fishValue[i] && neighborsNumber(population,population[fishID[j]])/populationNumber <= crowdFactor){
                    float rand = random.nextFloat();
                    double len = len(population[i], population[fishID[j]]);
                    for (int k = 0; k < dimNumber; k++) {
                        population[i][k] = population[i][k] + ((step[i] * rand) * (population[fishID[j]][k] - population[i][k]) / len )  ;
                    }
                    fishValue[i] = getFitness(population[i]);
                    follow_ok [i] = 1;
                }
                break;
            }

            if (follow_ok[i] != 1){
                population = prey(population[i],i);
            }
        }
        return population;
    }


    public static int[] getIndicesInOrder(double[] array) {
        Map<Integer, Double> map = new HashMap<Integer, Double>(array.length);
        for (int i = 0; i < array.length; i++)
            map.put(i, array[i]);

        List<Map.Entry<Integer, Double>> l = new ArrayList<Map.Entry<Integer, Double>>(map.entrySet());

        Collections.sort(l, new Comparator<Map.Entry<?, Double>>() {
            @Override
            public int compare(Map.Entry<?, Double> e1, Map.Entry<?, Double> e2) {
                return e2.getValue().compareTo(e1.getValue());
            }
        });

        int[] result = new int[array.length];
        for (int i = 0; i < result.length; i++)
            result[i] = l.get(i).getKey();
        return result;
    }


    public double[][] swarm() {
        for (int n = 0; n < populationNumber; n++){
            fishValue[n] = getFitness(population[n]);
        }

        double[] centerX     = center();
        double centerFitness = getFitness(centerX);

        for (int i = 0; i < populationNumber; i++) {
            double len = len(population[i], centerX);
            if (neighborsNumber(population,centerX) / populationNumber <= crowdFactor && centerFitness > getFitness(population[i])){
                float rand = random.nextFloat();
                for (int j = 0; j < dimNumber; j++) {
                    population[i][j] = population[i][j] + ((step[i] * rand)*(centerX[j] - population[i][j]) / len) ;
                }
                fishValue[i] = getFitness(population[i]);
            } else {
                population = prey(population[i],i);
            }
        }
        return population;
    }






    public double[][] prey( double[] xi, int n) {
        int findFood = 0;
        for (int i = 0; i < tryNumber; i++) {
            //
            double[] xj = xi.clone();
            for (int j = 0; j < dimNumber; j++) {
                double rand = (random.nextInt() % 2 == 0) ? random.nextDouble() : -1 * random.nextDouble();
                xj[j] = xi[j] + visual[n] * rand;
            }
            //
            if (getFitness(xj) > getFitness(xi)) {
                //
                double rand2 = random.nextDouble();
                double step = len(xi, xj);
                for (int k = 0; k < dimNumber ; k++) {
                    population[n][k] = xi[k] +  ((step * rand2)*(xj[k] - xi[k]) / (len(xj,xi)) )  ;
                }
                fishValue[n] = getFitness(population[n]);
                findFood     = 1;
                break;
            }
        }
        //
        if (findFood == 0){
            population = move(population,xi,n);
        }

        return population;
    }

    public double[][] move(double[][] population, double[] af, int m) {
        af = af.clone();
        for (int i = 0; i < dimNumber ; i++) {
            double rand = (random.nextInt() % 2 == 0) ? random.nextDouble() : -1 * random.nextDouble();
            population[m][i] = af[i] + step[m] * rand ;
        }
        fishValue[m] = getFitness(population[m]);
        return population;
    }

    private double[] center() {
        double[] sum = new double[dimNumber];
        for (int j = 0; j < dimNumber; j++) {
            for (int i = 0; i < populationNumber; i++) {
                sum[j] += population[i][j];
            }
            sum[j] /= populationNumber;
        }
        return sum;
    }

    public int neighborsNumber( double[][] p, double[] z){
        int nNum = 0;
        for(int i = 0; i < populationNumber; i++){
            if(len(p[i],z) <= visual[i] ){
                nNum++;
            }
        }
        return nNum;
    }

    private double len(double[] x, double[] y) {
        //
        double sum = 0;
        for (int i = 0; i < x.length; i++) {
            sum += Math.pow(x[i] - y[i], 2);
        }
        return Math.sqrt(sum);
    }

}