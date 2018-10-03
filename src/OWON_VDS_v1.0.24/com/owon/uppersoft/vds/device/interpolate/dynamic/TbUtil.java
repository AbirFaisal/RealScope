package com.owon.uppersoft.vds.device.interpolate.dynamic;

public class TbUtil {

	public static int[][] change(int pr, int line, int[] arrs) {
		int[][] arrss = new int[pr][line];
		for (int j = 0, k = 0; j < pr; j++) {
			System.out.print("{");
			int v;
			for (int i = 0; i < line; i++) {
				v = arrss[j][i] = arrs[k++];
				System.out.print(v + ", ");
			}
			System.out.println("},");
		}
		return arrss;
	}

	public static int[] sincT20 = { -1244, 7733, -32883, 987597, 52163, -17899,
			5476, -988, 0, -2403, 14814, -61569, 968193, 107916, -35990, 10908,
			-1959, 0, -3456, 21137, -85992, 942087, 166708, -53906, 16180,
			-2891, 0, -4386, 26616, -106138, 909648, 227948, -71270, 21177,
			-3765, 0, -5178, 31187, -122047, 871305, 291006, -87701, 25784,
			-4559, 0, -5820, 34802, -133805, 827549, 355221, -102814, 29895,
			-5257, 0, -6304, 37435, -141548, 778919, 419912, -116230, 33406,
			-5842, 0, -6626, 39078, -145453, 726002, 484381, -127577, 36224,
			-6298, 0, -6783, 39744, -145738, 669422, 547924, -136500, 38265,
			-6613, 0, -6778, 39458, -142658, 609836, 609836, -142658, 39458,
			-6778, 0, -6613, 38265, -136500, 547924, 669422, -145738, 39744,
			-6783, 0, -6298, 36224, -127577, 484381, 726002, -145453, 39078,
			-6626, 0, -5842, 33406, -116230, 419912, 778919, -141548, 37435,
			-6304, 0, -5257, 29895, -102814, 355221, 827549, -133805, 34802,
			-5820, 0, -4559, 25784, -87701, 291006, 871305, -122047, 31187,
			-5178, 0, -3765, 21177, -71270, 227948, 909648, -106138, 26616,
			-4386, 0, -2891, 16180, -53906, 166708, 942087, -85992, 21137,
			-3456, 0, -1959, 10908, -35990, 107916, 968193, -61569, 14814,
			-2403, 0, -988, 5476, -17899, 52163, 987597, -32883, 7733, -1244,
			0, 0, 0, 0, 0, 1000000, 0, 0, 0, 0

	};
	public static int[] sincT4 = { -5178, 31187, -122047, 871305, 291006,
			-87701, 25784, -4559, 0, -6778, 39458, -142658, 609836, 609836,
			-142658, 39458, -6778, 0, -4559, 25784, -87701, 291006, 871305,
			-122047, 31187, -5178, 0, 0, 0, 0, 0, 1000000, 0, 0, 0, 0 };

	public static int[] sincT2 = { -6778, 39458, -142658, 609836, 609836,
			-142658, 39458, -6778, 0, 0, 0, 0, 0, 1000000, 0, 0, 0, 0 };

	public static int[] sincT40 = { -631, 3941, -16960, 994689, 25600, -8903,
			2736, -495, 0, -1244, 7733, -32883, 987597, 52163, -17899, 5476,
			-988, 0, -1835, 11362, -47756, 978753, 79624, -26943, 8205, -1477,
			0, -2403, 14814, -61569, 968193, 107916, -35990, 10908, -1959, 0,
			-2944, 18076, -74315, 955956, 136968, -44993, 13572, -2431, 0,
			-3456, 21137, -85992, 942087, 166708, -53906, 16180, -2891, 0,
			-3937, 23987, -96598, 926634, 197061, -62681, 18720, -3337, 0,
			-4386, 26616, -106138, 909648, 227948, -71270, 21177, -3765, 0,
			-4800, 29019, -114618, 891185, 259290, -79626, 23536, -4173, 0,
			-5178, 31187, -122047, 871305, 291006, -87701, 25784, -4559, 0,
			-5518, 33116, -128437, 850071, 323011, -95446, 27908, -4921, 0,
			-5820, 34802, -133805, 827549, 355221, -102814, 29895, -5257, 0,
			-6082, 36242, -138168, 803807, 387550, -109758, 31731, -5565, 0,
			-6304, 37435, -141548, 778919, 419912, -116230, 33406, -5842, 0,
			-6486, 38380, -143968, 752958, 452218, -122185, 34907, -6087, 0,
			-6626, 39078, -145453, 726002, 484381, -127577, 36224, -6298, 0,
			-6725, 39532, -146033, 698129, 516313, -132363, 37346, -6474, 0,
			-6783, 39744, -145738, 669422, 547924, -136500, 38265, -6613, 0,
			-6801, 39717, -144602, 639963, 579128, -139945, 38971, -6715, 0,
			-6778, 39458, -142658, 609836, 609836, -142658, 39458, -6778, 0,
			-6715, 38971, -139945, 579128, 639963, -144602, 39717, -6801, 0,
			-6613, 38265, -136500, 547924, 669422, -145738, 39744, -6783, 0,
			-6474, 37346, -132363, 516313, 698129, -146033, 39532, -6725, 0,
			-6298, 36224, -127577, 484381, 726002, -145453, 39078, -6626, 0,
			-6087, 34907, -122185, 452218, 752958, -143968, 38380, -6486, 0,
			-5842, 33406, -116230, 419912, 778919, -141548, 37435, -6304, 0,
			-5565, 31731, -109758, 387550, 803807, -138168, 36242, -6082, 0,
			-5257, 29895, -102814, 355221, 827549, -133805, 34802, -5820, 0,
			-4921, 27908, -95446, 323011, 850071, -128437, 33116, -5518, 0,
			-4559, 25784, -87701, 291006, 871305, -122047, 31187, -5178, 0,
			-4173, 23536, -79626, 259290, 891185, -114618, 29019, -4800, 0,
			-3765, 21177, -71270, 227948, 909648, -106138, 26616, -4386, 0,
			-3337, 18720, -62681, 197061, 926634, -96598, 23987, -3937, 0,
			-2891, 16180, -53906, 166708, 942087, -85992, 21137, -3456, 0,
			-2431, 13572, -44993, 136968, 955956, -74315, 18076, -2944, 0,
			-1959, 10908, -35990, 107916, 968193, -61569, 14814, -2403, 0,
			-1477, 8205, -26943, 79624, 978753, -47756, 11362, -1835, 0, -988,
			5476, -17899, 52163, 987597, -32883, 7733, -1244, 0, -495, 2736,
			-8903, 25600, 994689, -16960, 3941, -631, 0, 0, 0, 0, 0, 1000000,
			0, 0, 0, 0 };
	public static int[] sincT25 = { -1001, 6235, -26640, 990646, 41426, -14292,
			4380, -791, 0, -1951, 12067, -50604, 976777, 85218, -28754, 8748,
			-1574, 0, -2838, 17439, -71852, 958536, 131100, -43198, 13043,
			-2338, 0, -3652, 22303, -90363, 936093, 178780, -57435, 17205,
			-3071, 0, -4386, 26616, -106138, 909648, 227948, -71270, 21177,
			-3765, 0, -5031, 30348, -119200, 879423, 278280, -84508, 24899,
			-4408, 0, -5582, 33472, -129592, 845668, 329439, -96952, 28317,
			-4991, 0, -6033, 35973, -137375, 808650, 381079, -108405, 31376,
			-5506, 0, -6382, 37842, -142630, 768659, 432846, -118676, 34027,
			-5944, 0, -6626, 39078, -145453, 726002, 484381, -127577, 36224,
			-6298, 0, -6765, 39688, -145959, 681000, 535323, -134926, 37922,
			-6562, 0, -6799, 39684, -144276, 633988, 585312, -140547, 39087,
			-6730, 0, -6730, 39087, -140547, 585312, 633988, -144276, 39684,
			-6799, 0, -6562, 37922, -134926, 535323, 681000, -145959, 39688,
			-6765, 0, -6298, 36224, -127577, 484381, 726002, -145453, 39078,
			-6626, 0, -5944, 34027, -118676, 432846, 768659, -142630, 37842,
			-6382, 0, -5506, 31376, -108405, 381079, 808650, -137375, 35973,
			-6033, 0, -4991, 28317, -96952, 329439, 845668, -129592, 33472,
			-5582, 0, -4408, 24899, -84508, 278280, 879423, -119200, 30348,
			-5031, 0, -3765, 21177, -71270, 227948, 909648, -106138, 26616,
			-4386, 0, -3071, 17205, -57435, 178780, 936093, -90363, 22303,
			-3652, 0, -2338, 13043, -43198, 131100, 958536, -71852, 17439,
			-2838, 0, -1574, 8748, -28754, 85218, 976777, -50604, 12067, -1951,
			0, -791, 4380, -14292, 41426, 990646, -26640, 6235, -1001, 0, 0, 0,
			0, 0, 1000000, 0, 0, 0, 0,

	};
	public static int[] sincT50 = { -506, 3164, -13651, 995895, 20401, -7114,
			2188, -396, 0, -1001, 6235, -26640, 990646, 41426, -14292, 4380,
			-791, 0, -1483, 9205, -38959, 984267, 63043, -21513, 6570, -1184,
			0, -1951, 12067, -50604, 976777, 85218, -28754, 8748, -1574, 0,
			-2403, 14814, -61569, 968193, 107916, -35990, 10908, -1959, 0,
			-2838, 17439, -71852, 958536, 131100, -43198, 13043, -2338, 0,
			-3255, 19937, -81450, 947828, 154734, -50354, 15144, -2709, 0,
			-3652, 22303, -90363, 936093, 178780, -57435, 17205, -3071, 0,
			-4030, 24530, -98591, 923357, 203198, -64415, 19219, -3424, 0,
			-4386, 26616, -106138, 909648, 227948, -71270, 21177, -3765, 0,
			-4720, 28557, -113006, 894993, 252990, -77976, 23073, -4093, 0,
			-5031, 30348, -119200, 879423, 278280, -84508, 24899, -4408, 0,
			-5318, 31987, -124727, 862971, 303778, -90841, 26650, -4707, 0,
			-5582, 33472, -129592, 845668, 329439, -96952, 28317, -4991, 0,
			-5820, 34802, -133805, 827549, 355221, -102814, 29895, -5257, 0,
			-6033, 35973, -137375, 808650, 381079, -108405, 31376, -5506, 0,
			-6221, 36987, -140313, 789007, 406969, -113700, 32756, -5735, 0,
			-6382, 37842, -142630, 768659, 432846, -118676, 34027, -5944, 0,
			-6517, 38539, -144338, 747644, 458665, -123310, 35185, -6132, 0,
			-6626, 39078, -145453, 726002, 484381, -127577, 36224, -6298, 0,
			-6709, 39461, -145988, 703773, 509949, -131457, 37138, -6442, 0,
			-6765, 39688, -145959, 681000, 535323, -134926, 37922, -6562, 0,
			-6795, 39761, -145383, 657724, 560459, -137963, 38573, -6658, 0,
			-6799, 39684, -144276, 633988, 585312, -140547, 39087, -6730, 0,
			-6778, 39458, -142658, 609836, 609836, -142658, 39458, -6778, 0,
			-6730, 39087, -140547, 585312, 633988, -144276, 39684, -6799, 0,
			-6658, 38573, -137963, 560459, 657724, -145383, 39761, -6795, 0,
			-6562, 37922, -134926, 535323, 681000, -145959, 39688, -6765, 0,
			-6442, 37138, -131457, 509949, 703773, -145988, 39461, -6709, 0,
			-6298, 36224, -127577, 484381, 726002, -145453, 39078, -6626, 0,
			-6132, 35185, -123310, 458665, 747644, -144338, 38539, -6517, 0,
			-5944, 34027, -118676, 432846, 768659, -142630, 37842, -6382, 0,
			-5735, 32756, -113700, 406969, 789007, -140313, 36987, -6221, 0,
			-5506, 31376, -108405, 381079, 808650, -137375, 35973, -6033, 0,
			-5257, 29895, -102814, 355221, 827549, -133805, 34802, -5820, 0,
			-4991, 28317, -96952, 329439, 845668, -129592, 33472, -5582, 0,
			-4707, 26650, -90841, 303778, 862971, -124727, 31987, -5318, 0,
			-4408, 24899, -84508, 278280, 879423, -119200, 30348, -5031, 0,
			-4093, 23073, -77976, 252990, 894993, -113006, 28557, -4720, 0,
			-3765, 21177, -71270, 227948, 909648, -106138, 26616, -4386, 0,
			-3424, 19219, -64415, 203198, 923357, -98591, 24530, -4030, 0,
			-3071, 17205, -57435, 178780, 936093, -90363, 22303, -3652, 0,
			-2709, 15144, -50354, 154734, 947828, -81450, 19937, -3255, 0,
			-2338, 13043, -43198, 131100, 958536, -71852, 17439, -2838, 0,
			-1959, 10908, -35990, 107916, 968193, -61569, 14814, -2403, 0,
			-1574, 8748, -28754, 85218, 976777, -50604, 12067, -1951, 0, -1184,
			6570, -21513, 63043, 984267, -38959, 9205, -1483, 0, -791, 4380,
			-14292, 41426, 990646, -26640, 6235, -1001, 0, -396, 2188, -7114,
			20401, 995895, -13651, 3164, -506, 0, 0, 0, 0, 0, 1000000, 0, 0, 0,
			0, };
	public static int[] sincT100 = { -254, 1593, -6908, 998091, 10121, -3547,
			1093, -198, 0, -506, 3164, -13651, 995895, 20401, -7114, 2188,
			-396, 0, -755, 4711, -20228, 993412, 30838, -10696, 3284, -594, 0,
			-1001, 6235, -26640, 990646, 41426, -14292, 4380, -791, 0, -1244,
			7733, -32883, 987597, 52163, -17899, 5476, -988, 0, -1483, 9205,
			-38959, 984267, 63043, -21513, 6570, -1184, 0, -1719, 10650,
			-44866, 980660, 74063, -25133, 7661, -1380, 0, -1951, 12067,
			-50604, 976777, 85218, -28754, 8748, -1574, 0, -2179, 13455,
			-56172, 972620, 96504, -32374, 9831, -1767, 0, -2403, 14814,
			-61569, 968193, 107916, -35990, 10908, -1959, 0, -2622, 16142,
			-66796, 963497, 119449, -39599, 11979, -2149, 0, -2838, 17439,
			-71852, 958536, 131100, -43198, 13043, -2338, 0, -3048, 18704,
			-76736, 953312, 142863, -46784, 14098, -2524, 0, -3255, 19937,
			-81450, 947828, 154734, -50354, 15144, -2709, 0, -3456, 21137,
			-85992, 942087, 166708, -53906, 16180, -2891, 0, -3652, 22303,
			-90363, 936093, 178780, -57435, 17205, -3071, 0, -3844, 23434,
			-94562, 929849, 190945, -60939, 18219, -3249, 0, -4030, 24530,
			-98591, 923357, 203198, -64415, 19219, -3424, 0, -4211, 25591,
			-102450, 916623, 215534, -67860, 20205, -3596, 0, -4386, 26616,
			-106138, 909648, 227948, -71270, 21177, -3765, 0, -4556, 27605,
			-109657, 902437, 240435, -74644, 22133, -3930, 0, -4720, 28557,
			-113006, 894993, 252990, -77976, 23073, -4093, 0, -4878, 29471,
			-116187, 887321, 265606, -81265, 23995, -4252, 0, -5031, 30348,
			-119200, 879423, 278280, -84508, 24899, -4408, 0, -5178, 31187,
			-122047, 871305, 291006, -87701, 25784, -4559, 0, -5318, 31987,
			-124727, 862971, 303778, -90841, 26650, -4707, 0, -5453, 32749,
			-127241, 854423, 316591, -93926, 27494, -4851, 0, -5582, 33472,
			-129592, 845668, 329439, -96952, 28317, -4991, 0, -5704, 34157,
			-131780, 836708, 342318, -99915, 29117, -5126, 0, -5820, 34802,
			-133805, 827549, 355221, -102814, 29895, -5257, 0, -5930, 35407,
			-135670, 818195, 368143, -105645, 30648, -5384, 0, -6033, 35973,
			-137375, 808650, 381079, -108405, 31376, -5506, 0, -6130, 36500,
			-138922, 798919, 394023, -111091, 32079, -5623, 0, -6221, 36987,
			-140313, 789007, 406969, -113700, 32756, -5735, 0, -6304, 37435,
			-141548, 778919, 419912, -116230, 33406, -5842, 0, -6382, 37842,
			-142630, 768659, 432846, -118676, 34027, -5944, 0, -6453, 38211,
			-143559, 758232, 445766, -121037, 34621, -6040, 0, -6517, 38539,
			-144338, 747644, 458665, -123310, 35185, -6132, 0, -6575, 38829,
			-144969, 736899, 471539, -125491, 35720, -6218, 0, -6626, 39078,
			-145453, 726002, 484381, -127577, 36224, -6298, 0, -6671, 39289,
			-145792, 714958, 497186, -129567, 36696, -6373, 0, -6709, 39461,
			-145988, 703773, 509949, -131457, 37138, -6442, 0, -6740, 39594,
			-146043, 692452, 522663, -133244, 37546, -6505, 0, -6765, 39688,
			-145959, 681000, 535323, -134926, 37922, -6562, 0, -6783, 39744,
			-145738, 669422, 547924, -136500, 38265, -6613, 0, -6795, 39761,
			-145383, 657724, 560459, -137963, 38573, -6658, 0, -6800, 39741,
			-144895, 645911, 572924, -139313, 38847, -6698, 0, -6799, 39684,
			-144276, 633988, 585312, -140547, 39087, -6730, 0, -6792, 39589,
			-143530, 621962, 597618, -141663, 39290, -6757, 0, -6778, 39458,
			-142658, 609836, 609836, -142658, 39458, -6778, 0, -6757, 39290,
			-141663, 597618, 621962, -143530, 39589, -6792, 0, -6730, 39087,
			-140547, 585312, 633988, -144276, 39684, -6799, 0, -6698, 38847,
			-139313, 572924, 645911, -144895, 39741, -6800, 0, -6658, 38573,
			-137963, 560459, 657724, -145383, 39761, -6795, 0, -6613, 38265,
			-136500, 547924, 669422, -145738, 39744, -6783, 0, -6562, 37922,
			-134926, 535323, 681000, -145959, 39688, -6765, 0, -6505, 37546,
			-133244, 522663, 692452, -146043, 39594, -6740, 0, -6442, 37138,
			-131457, 509949, 703773, -145988, 39461, -6709, 0, -6373, 36696,
			-129567, 497186, 714958, -145792, 39289, -6671, 0, -6298, 36224,
			-127577, 484381, 726002, -145453, 39078, -6626, 0, -6218, 35720,
			-125491, 471539, 736899, -144969, 38829, -6575, 0, -6132, 35185,
			-123310, 458665, 747644, -144338, 38539, -6517, 0, -6040, 34621,
			-121037, 445766, 758232, -143559, 38211, -6453, 0, -5944, 34027,
			-118676, 432846, 768659, -142630, 37842, -6382, 0, -5842, 33406,
			-116230, 419912, 778919, -141548, 37435, -6304, 0, -5735, 32756,
			-113700, 406969, 789007, -140313, 36987, -6221, 0, -5623, 32079,
			-111091, 394023, 798919, -138922, 36500, -6130, 0, -5506, 31376,
			-108405, 381079, 808650, -137375, 35973, -6033, 0, -5384, 30648,
			-105645, 368143, 818195, -135670, 35407, -5930, 0, -5257, 29895,
			-102814, 355221, 827549, -133805, 34802, -5820, 0, -5126, 29117,
			-99915, 342318, 836708, -131780, 34157, -5704, 0, -4991, 28317,
			-96952, 329439, 845668, -129592, 33472, -5582, 0, -4851, 27494,
			-93926, 316591, 854423, -127241, 32749, -5453, 0, -4707, 26650,
			-90841, 303778, 862971, -124727, 31987, -5318, 0, -4559, 25784,
			-87701, 291006, 871305, -122047, 31187, -5178, 0, -4408, 24899,
			-84508, 278280, 879423, -119200, 30348, -5031, 0, -4252, 23995,
			-81265, 265606, 887321, -116187, 29471, -4878, 0, -4093, 23073,
			-77976, 252990, 894993, -113006, 28557, -4720, 0, -3930, 22133,
			-74644, 240435, 902437, -109657, 27605, -4556, 0, -3765, 21177,
			-71270, 227948, 909648, -106138, 26616, -4386, 0, -3596, 20205,
			-67860, 215534, 916623, -102450, 25591, -4211, 0, -3424, 19219,
			-64415, 203198, 923357, -98591, 24530, -4030, 0, -3249, 18219,
			-60939, 190945, 929849, -94562, 23434, -3844, 0, -3071, 17205,
			-57435, 178780, 936093, -90363, 22303, -3652, 0, -2891, 16180,
			-53906, 166708, 942087, -85992, 21137, -3456, 0, -2709, 15144,
			-50354, 154734, 947828, -81450, 19937, -3255, 0, -2524, 14098,
			-46784, 142863, 953312, -76736, 18704, -3048, 0, -2338, 13043,
			-43198, 131100, 958536, -71852, 17439, -2838, 0, -2149, 11979,
			-39599, 119449, 963497, -66796, 16142, -2622, 0, -1959, 10908,
			-35990, 107916, 968193, -61569, 14814, -2403, 0, -1767, 9831,
			-32374, 96504, 972620, -56172, 13455, -2179, 0, -1574, 8748,
			-28754, 85218, 976777, -50604, 12067, -1951, 0, -1380, 7661,
			-25133, 74063, 980660, -44866, 10650, -1719, 0, -1184, 6570,
			-21513, 63043, 984267, -38959, 9205, -1483, 0, -988, 5476, -17899,
			52163, 987597, -32883, 7733, -1244, 0, -791, 4380, -14292, 41426,
			990646, -26640, 6235, -1001, 0, -594, 3284, -10696, 30838, 993412,
			-20228, 4711, -755, 0, -396, 2188, -7114, 20401, 995895, -13651,
			3164, -506, 0, -198, 1093, -3547, 10121, 998091, -6908, 1593, -254,
			0, 0, 0, 0, 0, 1000000, 0, 0, 0, 0, };
	static {
		TbUtil.change(2, 9, TbUtil.sincT2);
	}

	public static void main(String[] args) {

	}

}