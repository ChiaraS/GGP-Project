package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.utils;

public class Utils {
    /**
     * Map the value in R to [lowerBound, upperBound]
     * @param upperBound
     * @param lowerBound
     * @param valueInR
     * @return
     */
    public double mapToInterval(double upperBound, double lowerBound, double valueInR) {
        //double normalised = 0.0;

        return ( mapToOne(valueInR)*(upperBound-lowerBound) + lowerBound );
    }

    /**
     * Map the value in R to (0, 1)
     * @param valueInR
     * @return
     */
    public double mapToOne(double valueInR) {
        return ( ( valueInR / Math.sqrt(1+valueInR*valueInR) + 1 ) / 2 );
//        return Math.exp(valueInR) / (1 + Math.exp(valueInR));
    }

    public void heapSort(double[] input) {
        int n = input.length;

        // Build heap (rearrange array)
        for (int i = n / 2 - 1; i >= 0; i--)
            heapify(input, n, i);

        // One by one extract an element from heap
        for (int i=n-1; i>=0; i--)
        {
            // Move current root to end
            double temp = input[0];
            input[0] = input[i];
            input[i] = temp;

            // call max heapify on the reduced heap
            heapify(input, i, 0);
        }
    }

    void heapify(double[] arr, int n, int i)
    {
        int largest = i;  // Initialize largest as root
        int l = 2*i + 1;  // left = 2*i + 1
        int r = 2*i + 2;  // right = 2*i + 2

        // If left child is larger than root
        if (l < n && arr[l] > arr[largest])
            largest = l;

        // If right child is larger than largest so far
        if (r < n && arr[r] > arr[largest])
            largest = r;

        // If largest is not root
        if (largest != i)
        {
            double swap = arr[i];
            arr[i] = arr[largest];
            arr[largest] = swap;

            // Recursively heapify the affected sub-tree
            heapify(arr, n, largest);
        }
    }

}
