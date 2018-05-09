
//import com.google.common.hash.HashFunction;
import com.google.common.base.Charsets;
import com.google.common.hash.*;


public class HyperLogLog {
    private int[] buckets;
    private double currentSum;
    private int nonZeroBuckets = 0;
    private HashFunction HASH;
    private long count = 0;
    private long totalStringBytes = 0;

    public HyperLogLog(int numberOfBuckets) {
        if (!isPowerOfTwo(numberOfBuckets)) {
            System.out.println("numberOfBuckets must be a power of two");
        }
        buckets = new int[numberOfBuckets];
        currentSum = buckets.length;
        HASH = Hashing.murmur3_128();
    }

    private boolean isPowerOfTwo(int number) {
        int setBit = 0;
        while (number > 0) {
            setBit++;
            number &= number - 1;
        }
        return setBit == 1;
    }
    int[] getBuckets() {
        return buckets;
    }
    long getHash(long input) {
        return HASH.hashLong(input).asLong();
    }
    long getHash(String input) {return HASH.hashString(input, Charsets.UTF_8).asLong();}
    int getBucket(long hash) {
        int bucketMask = buckets.length - 1;
        return (int) (hash & bucketMask);
    }
    long getHashCode(long hash) {
        int bits = Integer.numberOfTrailingZeros(buckets.length);
        return hash >> bits;
    }

    public void add(long input) {
        count++;
        long hash = getHash(input);
        int bucket = getBucket(hash);

        int lowestBit = Long.numberOfTrailingZeros(getHashCode(hash)) + 1;
        int previous = buckets[bucket];

        if (previous == 0)
            nonZeroBuckets++;
        if (lowestBit > previous) {
            currentSum -= 1.0 / (1L << previous);
            currentSum += 1.0 / (1L << lowestBit);
            buckets[bucket] = lowestBit;
        }
    }
    public void add(String input) {
        count++;
        totalStringBytes += input.length();
        long hash = getHash(input);
        int bucket = getBucket(hash);

        int lowestBit = Long.numberOfTrailingZeros(getHashCode(hash)) + 1;
        int previous = buckets[bucket];

        if (previous == 0)
            nonZeroBuckets++;
        if (lowestBit > previous) {
            currentSum -= 1.0 / (1L << previous);
            currentSum += 1.0 / (1L << lowestBit);
            buckets[bucket] = lowestBit;
        }
    }
    public long estimate() {
        double alpha = computeAlpha(buckets.length);
        double result = alpha * buckets.length * buckets.length / currentSum;

        if (result <= 2.5 * buckets.length) {
            // adjust for small cardinalities
            int zeroBuckets = buckets.length - nonZeroBuckets;
            if (zeroBuckets > 0) {
                result = buckets.length * Math.log(buckets.length * 1.0 / zeroBuckets);
            }
        }
        return Math.round(result);
    }
    private double computeAlpha(int numberOfBuckets) {
        double alpha;
        switch (numberOfBuckets) {
            case (1 << 4):
                alpha = 0.673;
                break;
            case (1 << 5):
                alpha = 0.697;
                break;
            case (1 << 6):
                alpha = 0.709;
                break;
            default:
                alpha = (0.7213 / (1 + 1.079 / numberOfBuckets));
        }
        return alpha;
    }
    long getCount() {
        return count;
    }
    long getTotalStringBytes() {
        return totalStringBytes;
    }
}