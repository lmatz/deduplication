package MyDedup;

import java.util.Random;

public class ModExp {
	
	// copy from the tutorial notes
	
	public static long modExp( long d, int e, int q ) {
		// Compute d^e mod q
		long result = 1;
		while( e > 0 ) {
			if ( e % 2 == 1 )
				result = ( result * d ) % q;
			e = ( e - ( e % 2 ) ) / 2;
			d = ( d * d ) % q;
		}
		return result;
	}
	
	public static long modExpOpt( long d, int e, int q ) {
		// Compute d^e mod q
		long result = 1;
		while( e > 0 ) {
			if ( ( e & 1 ) == 1 )
				result = ( result * d ) % q;
			e = ( e - ( e & 1 ) ) >> 1;
			d = ( d * d ) % q;
		}
		return result;
	}

	public static void main( String[] args ) {
		long startTime, endTime;
		double duration, improvement;
		Random rand = new Random();
		int[] ds = new int[ 1000 ];
		int[] es = new int[ 1000 ];
		int[] qs = new int[ 1000 ];
		int[] anss = new int[ 1000 ];
		int[] anssOpt = new int[ 1000 ];

		for ( int i = 0; i < 1000; i++ ) {
			ds[ i ] = rand.nextInt( 100 ) + 2;
			es[ i ] = rand.nextInt( 1000 ) + 100;
			qs[ i ] = rand.nextInt( 1000 ) + 3;
		}

		startTime = System.nanoTime();
		for ( int loop = 0; loop < 100000; loop++ ) {
			for ( int i = 0; i < 1000; i++ ) {
				anss[ i ] = ( int ) modExp( ds[ i ], es[ i ], qs[ i ] );
			}
		}
		endTime = System.nanoTime();
		duration = ( (double) endTime - startTime ) * 1e-9;
		System.out.println( "Time elapsed of original implementation: " + duration + " s." ) ;
		
		startTime = System.nanoTime();
		for ( int loop = 0; loop < 100000; loop++ ) {
			for ( int i = 0; i < 1000; i++ ) {
				anssOpt[ i ] = ( int ) modExpOpt( ds[ i ], es[ i ], qs[ i ] );
			}
		}
		endTime = System.nanoTime();
		improvement = duration;
		duration = ( (double) endTime - startTime ) * 1e-9;
		improvement = ( improvement - duration ) / improvement * 100;
		System.out.println( "Time elapsed of optimized implementation: " + duration + " s." ) ;
		System.out.println( "Improvement: " + improvement + " %." ) ;
		
		for ( int i = 0; i < 1000; i++ ) {
			if ( anss[ i ] != anssOpt[ i ] )
				System.err.println( "Mismatch!" );
			// System.out.println( "ans[ " + i + " ]" + " = " + anss[ i ] );
		}

		// System.out.println( modExp( 3, 101, 11 ) );
		// System.out.println( modExpOpt( 3, 101, 11 ) );
		// System.out.println( modExp( 7, 123, 17 ) );
		// System.out.println( modExpOpt( 7, 123, 17 ) );
	}
}
