

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;



public class CvrpAnneal {
	private static int clientSum;// the sum of numbers for clients
	private static Random random = new Random();
	private static int capacity;
	
	
	
	public CvrpAnneal(){

	}
	
	
	public double evaluate(int route[],CVRPData data){
		double distance = 0.0;
		int truckCapacity= 500;
		int demand;
		
		for(int i = 0;i<clientSum-1;i++){
			demand = data.getDemand(route[i+1]);
			if(demand<=truckCapacity){
				truckCapacity = truckCapacity - demand;  
				distance +=  data.getDistance(route[i], route[i+1]);
				if(i == 248)
					distance += data.getDistance(route[i+1], route[250]);
			}
			else{
				distance += data.getDistance(route[i], route[250]);
				distance += data.getDistance(route[0], route[i+1]);
				truckCapacity = 500 - demand;
				if(i == 248)
					distance += data.getDistance(route[i+1], route[250]);
			}

		}
		
		return distance;
	}	
	

	public int[] createRoute(int[] currentRoute) {
		int[] neighborRoute = new int[clientSum+1];
		int[] newRoute = new int[clientSum+1];
		
		for(int i =0;i<neighborRoute.length;i++){
			neighborRoute[i] = currentRoute[i];
		}
		int n;
		int x=random.nextInt(neighborRoute.length-2)+1;//1-249
		int y;
		do{
		   y=random.nextInt(neighborRoute.length-2)+1;// not equal to x
		}while(y == x);

		
		/*I write 3 methods below for creating new route :
		 *    1.simple permutation
		 *    2.side permutation
		 *    3.inverse permutation
		 *After testing, the third one is the best. The crossover is the worst
		 * */
		
//		//simple permutation
//		newRoute = simpPermutate(neighborRoute,x,y);

		
//		//sides permutation
//		newRoute = sidePermutation(neighborRoute,x,y);
		
		//inverse permutation
		newRoute = invPermutate(neighborRoute,x,y);
		             
		return newRoute;
	}
	
        //simple permutation
	private int[] simpPermutate(int[] neighborRoute, int x, int y) {
		int n;
		n = neighborRoute[x];
		neighborRoute[x] = neighborRoute[y];
		neighborRoute[y] = n;
		
		return neighborRoute;
	}


	//inverse permutation of the number between x and y
	private int[] invPermutate(int[] route, int x, int y) {
		int temp;
		if(x>y){
			temp = x; x = y; y = temp;
		}
		
		int n = y-x+1;
        int []inverse = new int[n];
			
		for(int i=0;i<n;i++){
            inverse[i] = route[x];
			x++;
		}
		
		for(int i=0;i<n;i++){
			route[y] = inverse[i];
			y--;
		}
		return route;
	}

	private int[] sidePermutation(int[] route, int x, int y) {
		ArrayList<Integer> cross = new ArrayList<Integer>();
		int[] newRoute = new int[clientSum+1];
		int temp;
		if(x>y){
			temp = x; x = y; y = temp;
		}
        cross.add(1);
		for(int i = y+1;i<route.length-1;i++){
		    cross.add(route[i]);
		}
		
		for(int i = x;i<y+1;i++){
			cross.add(route[i]);
		}
		
		if(x!=1){
			for(int i = 1;i<x;i++){
				cross.add(route[i]);
			}
		}
		cross.add(1);
		
		for(int i =0;i<newRoute.length;i++){
			newRoute[i] = cross.get(i);
		}
		
		//mutation
		int n;
		n = newRoute[x];
		newRoute[x] = newRoute[y];
		newRoute[y] = n;
		
		return newRoute;
	}


	public void showRoute(int[] bestRoute,CVRPData data) {
		int truckCapacity= capacity;
		int demand;
		double bestDis;
		
		for(int i = 0;i<249;i++){
			demand = data.getDemand(bestRoute[i+1]);
			if(demand<=truckCapacity){
				truckCapacity = truckCapacity - demand;  
				System.out.print(bestRoute[i] + "->");
				if(i == 248)
					System.out.println(bestRoute[i+1] + "->1");
			}
			else{
				System.out.print(bestRoute[i] + "->1");
				System.out.println("");
				System.out.print(bestRoute[0] + "->");
				truckCapacity = capacity - demand;
				if(i == 248)
					System.out.println(bestRoute[i+1] + "->1");
			}
		}
		
		bestDis = evaluate(bestRoute,data);
	}


	
	public static void runAnneal(){

		CVRPData data = new CVRPData();
		data.readFile("./fruitybun250.vrp");
		clientSum = data.NUM_NODES;//250
		capacity = data.VEHICLE_CAPACITY;//500
		
		CvrpAnneal cvrpAnneal = new CvrpAnneal();
		
		/*
		* evaluate(int[])：calculate the temperature(evaluation,distance) of the status
		* route[i)]：current status
		* neighbor[i+1]：next status
		* bestRoute : best status
		* r： for controlling the speed for annealing
		* T： the initial temperature which should be large
		* T_min : the min of temperature for stop the annealing
		* dE : the temperature variation
		*/
		int currentRoute1[] = new int[clientSum+1];
		int newRoute1[] = new int[clientSum+1];
		int bestRoute1[] = new int[clientSum+1];
		double currentRouteDis1,newRouteDis1,bestRouteDis1;
		int currentRoute2[] = new int[clientSum+1];
		int newRoute2[] = new int[clientSum+1];
		int bestRoute2[] = new int[clientSum+1];
		double currentRouteDis2,newRouteDis2,bestRouteDis2;
		
		int matchRoute[] = new int[clientSum+1];
		double matchRouteDis =0.0;
		
		double T = 20.0;
		int T_min = 1;
		double r = 0.999999900142263;
		double dE1,dE2;
		int loopNum = 1;
        
		for(int i=0; i<clientSum; i++) {
			currentRoute1[i] = i+1;
			bestRoute1[i] = i+1;
			currentRoute2[i] = i+1;
			bestRoute2[i] = i+1;
		}	
		currentRoute1[clientSum] = 1;
		bestRoute1[clientSum] = 1;
		currentRoute2[clientSum] = 1;
		bestRoute2[clientSum] = 1;
		
		currentRouteDis1 = cvrpAnneal.evaluate(currentRoute1,data);	
		bestRouteDis1 = currentRouteDis1;		
		currentRouteDis2 = cvrpAnneal.evaluate(currentRoute2,data);	
		bestRouteDis2 = currentRouteDis2;
		
		//begin to anneal
		while(T>=T_min){
			newRoute1 = cvrpAnneal.createRoute(currentRoute1);
			newRouteDis1 = cvrpAnneal.evaluate(newRoute1,data);
			newRoute2 = cvrpAnneal.createRoute(currentRoute2);
			newRouteDis2 = cvrpAnneal.evaluate(newRoute2,data);
			
			if(newRouteDis1 < bestRouteDis1){
				for(int i=0;i<bestRoute1.length;i++){
					bestRoute1[i] = newRoute1[i];
				}
				bestRouteDis1 = newRouteDis1;
			}
			
			if(newRouteDis2 < bestRouteDis2){
				for(int i=0;i<bestRoute2.length;i++){
					bestRoute2[i] = newRoute2[i];
				}
				bestRouteDis2 = newRouteDis2;
			}
			
			double randD1 = random.nextDouble();
			double randD2 = random.nextDouble();
			
			dE1 = currentRouteDis1 - newRouteDis1;
			dE2 = currentRouteDis2 - newRouteDis2;
			
			if(dE1>=0){
				currentRouteDis1 = newRouteDis1;
				for(int i=0;i<currentRoute1.length;i++){
					currentRoute1[i] = newRoute1[i];
				}
			}
			else if(dE1<0 && Math.exp(dE1/ T) > randD1){
					currentRouteDis1 = newRouteDis1;
					for(int i=0;i<currentRoute1.length;i++){
						currentRoute1[i] = newRoute1[i];
					}
			}
			
			if(dE2>=0){
				currentRouteDis2 = newRouteDis2;
				for(int i=0;i<currentRoute2.length;i++){
					currentRoute2[i] = newRoute2[i];
				}
			}
			else if(dE2<0 && Math.exp(dE2/ T) > randD2){
					currentRouteDis2 = newRouteDis2;
					for(int i=0;i<currentRoute2.length;i++){
						currentRoute2[i] = newRoute2[i];
					}
			}
			
			//to get the best match route from two partials
			if(bestRouteDis1>bestRouteDis2){
				matchRouteDis = bestRouteDis2;
				for(int i=0;i<matchRoute.length;i++){
					matchRoute[i] = bestRoute2[i];
				}
			}else{
				matchRouteDis = bestRouteDis1;
				for(int i=0;i<matchRoute.length;i++){
					matchRoute[i] = bestRoute1[i];
				}
			}
			
			System.out.println(matchRouteDis);
			T = T*r;
		}
		
		System.out.println("login bz16563 33498");
		System.out.println("name Boyang Zhao");
		System.out.println("algorithm particle swarm optimization and simulated annealing Algorithm");
		System.out.println("cost " + matchRouteDis);
		cvrpAnneal.showRoute(matchRoute,data);
		Date date = new Date();
		System.out.println("\n"+date.getHours()+":"+date.getMinutes()+":"+date.getSeconds());
	}
	




	public static void main(String args[]){
		System.out.println("bz16563  Boyang Zhao  start...");
		runAnneal();		
	}
	

}
