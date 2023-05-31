package io.github.icodegarden.commons.redis;

import java.util.List;

import io.github.icodegarden.commons.lang.annotation.NotNull;
import io.github.icodegarden.commons.lang.annotation.Nullable;
import io.github.icodegarden.commons.redis.args.GeoAddArgs;
import io.github.icodegarden.commons.redis.args.GeoArgs;
import io.github.icodegarden.commons.redis.args.GeoCoordinate;
import io.github.icodegarden.commons.redis.args.GeoRadiusStoreArgs;
import io.github.icodegarden.commons.redis.args.GeoSearch;
import io.github.icodegarden.commons.redis.args.GeoUnit;
import io.github.icodegarden.commons.redis.args.GeoValue;
import io.github.icodegarden.commons.redis.args.GeoWithin;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface GeoBinaryCommands {

	long geoadd(byte[] key, double longitude, double latitude, byte[] member);

	long geoadd(byte[] key, double longitude, double latitude, byte[] member, GeoAddArgs args);

	@SuppressWarnings("unchecked")
	long geoadd(byte[] key, GeoValue<byte[]>... geoValues);

	/**
	 * <h1>添加一个或多个对象的地理位置</h1><br>
	 * GEOADD key [NX | XX] [CH] longitude latitude member [longitude latitude
	 * member ...]<br>
	 * 
	 * Adds the specified geospatial items (longitude, latitude, name) to the
	 * specified key. Data is stored into the key as a sorted set, in a way that
	 * makes it possible to query the items with the GEOSEARCH command.
	 * 
	 * The command takes arguments in the standard format x,y so the longitude must
	 * be specified before the latitude. There are limits to the coordinates that
	 * can be indexed: areas very near to the poles are not indexable.
	 * 
	 * The exact limits, as specified by EPSG:900913 / EPSG:3785 / OSGEO:41001 are
	 * the following:
	 * 
	 * Valid longitudes are from -180 to 180 degrees. Valid latitudes are from
	 * -85.05112878 to 85.05112878 degrees. The command will report an error when
	 * the user attempts to index coordinates outside the specified ranges.
	 * 
	 * Note: there is no GEODEL command because you can use ZREM to remove elements.
	 * The Geo index structure is just a sorted set.
	 * 
	 * redis> GEOADD Sicily 13.361389 38.115556 "Palermo" 15.087269 37.502669
	 * "Catania"<br>
	 * (integer) 2<br>
	 * redis> GEODIST Sicily Palermo Catania<br>
	 * "166274.1516"<br>
	 * redis> GEORADIUS Sicily 15 37 100 km<br>
	 * 1) "Catania"<br>
	 * redis> GEORADIUS Sicily 15 37 200 km<br>
	 * 1) "Palermo"<br>
	 * 2) "Catania"<br>
	 * redis> <br>
	 * 
	 * @param key
	 * @param args
	 * @param geoValues
	 * @return
	 */
	@SuppressWarnings("unchecked")
	long geoadd(byte[] key, GeoAddArgs args, GeoValue<byte[]>... geoValues);

	@Nullable
	Double geodist(byte[] key, byte[] member1, byte[] member2);

	/**
	 * <h1>返回2个对象之间的地理距离</h1><br>
	 * GEODIST key member1 member2 [M | KM | FT | MI]
	 * 
	 * Return the distance between two members in the geospatial index represented
	 * by the sorted set.
	 * 
	 * Given a sorted set representing a geospatial index, populated using the
	 * GEOADD command, the command returns the distance between the two specified
	 * members in the specified unit.
	 * 
	 * If one or both the members are missing, the command returns NULL.
	 * 
	 * The unit must be one of the following, and defaults to meters:
	 * 
	 * m for meters.<br>
	 * km for kilometers.<br>
	 * mi for miles.<br>
	 * ft for feet.<br>
	 * The distance is computed assuming that the Earth is a perfect sphere, so
	 * errors up to 0.5% are possible in edge cases.<br>
	 * 
	 * redis> GEOADD Sicily 13.361389 38.115556 "Palermo" 15.087269 37.502669
	 * "Catania"<br>
	 * (integer) 2<br>
	 * redis> GEODIST Sicily Palermo Catania<br>
	 * "166274.1516"<br>
	 * redis> GEODIST Sicily Palermo Catania km<br>
	 * "166.2742"<br>
	 * redis> GEODIST Sicily Palermo Catania mi<br>
	 * "103.3182"<br>
	 * redis> GEODIST Sicily Foo Bar<br>
	 * (nil)<br>
	 * redis> <br>
	 * 
	 * @param key
	 * @param member1
	 * @param member2
	 * @param unit
	 * @returnBulk string reply, specifically:
	 * 
	 *             The command returns the distance as a double (represented as a
	 *             string) in the specified unit, or NULL if one or both the
	 *             elements are missing.
	 */
	@Nullable
	Double geodist(byte[] key, byte[] member1, byte[] member2, GeoUnit unit);

	/**
	 * <h1>返回对象之间的距离hash.</h1><br>
	 * GEOHASH key [member [member ...]]
	 * 
	 * Return valid Geohash strings representing the position of one or more
	 * elements in a sorted set value representing a geospatial index (where
	 * elements were added using GEOADD).
	 * 
	 * Normally Redis represents positions of elements using a variation of the
	 * Geohash technique where positions are encoded using 52 bit integers. The
	 * encoding is also different compared to the standard because the initial min
	 * and max coordinates used during the encoding and decoding process are
	 * different. This command however returns a standard Geohash in the form of a
	 * string as described in the Wikipedia article and compatible with the
	 * geohash.org web site.
	 * 
	 * redis> GEOADD Sicily 13.361389 38.115556 "Palermo" 15.087269 37.502669
	 * "Catania"<br>
	 * (integer) 2<br>
	 * redis> GEOHASH Sicily Palermo Catania<br>
	 * 1) "sqc8b49rny0"<br>
	 * 2) "sqdtr74hyu0"<br>
	 * redis> <br>
	 * 
	 * @param key
	 * @param members
	 * @return Array reply, specifically:
	 * 
	 *         The command returns an array where each element is the Geohash
	 *         corresponding to each member name passed as argument to the command.
	 */
	@NotNull
	List<String> geohash(byte[] key, byte[]... members);

	/**
	 * <h1>返回对象的地理位置</h1><br>
	 * GEOPOS key [member [member ...]]
	 * 
	 * Return the positions (longitude,latitude) of all the specified members of the
	 * geospatial index represented by the sorted set at key.
	 * 
	 * Given a sorted set representing a geospatial index, populated using the
	 * GEOADD command, it is often useful to obtain back the coordinates of
	 * specified members. When the geospatial index is populated via GEOADD the
	 * coordinates are converted into a 52 bit geohash, so the coordinates returned
	 * may not be exactly the ones used in order to add the elements, but small
	 * errors may be introduced.
	 * 
	 * The command can accept a variable number of arguments so it always returns an
	 * array of positions even when a single element is specified.
	 * 
	 * redis> GEOADD Sicily 13.361389 38.115556 "Palermo" 15.087269 37.502669
	 * "Catania"<br>
	 * (integer) 2<br>
	 * redis> GEOPOS Sicily Palermo Catania NonExisting<br>
	 * 1) 1) "13.36138933897018433"<br>
	 * 2) "38.11555639549629859"<br>
	 * 2) 1) "15.08726745843887329"<br>
	 * 2) "37.50266842333162032"<br>
	 * 3) (nil)<br>
	 * redis> <br>
	 * 
	 * @param key
	 * @param members
	 * @return Array reply, specifically:
	 * 
	 *         The command returns an array where each element is a two elements
	 *         array representing longitude and latitude (x,y) of each member name
	 *         passed as argument to the command.
	 * 
	 *         Non existing elements are reported as NULL elements of the array.
	 */
	@NotNull
	List<GeoCoordinate> geopos(byte[] key, byte[]... members);

	/**
	 * @return members
	 */
	@NotNull
	List<byte[]> georadius(byte[] key, double longitude, double latitude, double radius, GeoUnit unit);

	@NotNull
	List<GeoWithin<byte[]>> georadius(byte[] key, double longitude, double latitude, double radius, GeoUnit unit,
			GeoArgs args);

	/**
	 * <h1>查询距离坐标一定距离内成员的地理空间索引，并存储结果（可选）</h1><br>
	 * GEORADIUS key longitude latitude radius <M | KM | FT | MI><br>
	 * [WITHCOORD] [WITHDIST] [WITHHASH] [COUNT count [ANY]] [ASC | DESC]<br>
	 * [STORE key] [STOREDIST key]<br>
	 * 
	 * Return the members of a sorted set populated with geospatial information
	 * using GEOADD, which are within the borders of the area specified with the
	 * center location and the maximum distance from the center (the radius).
	 * 
	 * This manual page also covers the GEORADIUS_RO and GEORADIUSBYMEMBER_RO
	 * variants (see the section below for more information).
	 * 
	 * The common use case for this command is to retrieve geospatial items near a
	 * specified point not farther than a given amount of meters (or other units).
	 * This allows, for example, to suggest mobile users of an application nearby
	 * places.
	 * 
	 * The radius is specified in one of the following units:
	 * 
	 * m for meters.<br>
	 * km for kilometers.<br>
	 * mi for miles.<br>
	 * ft for feet.<br>
	 * The command optionally returns additional information using the following
	 * options:<br>
	 * 
	 * WITHDIST: Also return the distance of the returned items from the specified
	 * center. The distance is returned in the same unit as the unit specified as
	 * the radius argument of the command. WITHCOORD: Also return the
	 * longitude,latitude coordinates of the matching items. WITHHASH: Also return
	 * the raw geohash-encoded sorted set score of the item, in the form of a 52 bit
	 * unsigned integer. This is only useful for low level hacks or debugging and is
	 * otherwise of little interest for the general user. The command default is to
	 * return unsorted items. Two different sorting methods can be invoked using the
	 * following two options:
	 * 
	 * ASC: Sort returned items from the nearest to the farthest, relative to the
	 * center. DESC: Sort returned items from the farthest to the nearest, relative
	 * to the center. By default all the matching items are returned. It is possible
	 * to limit the results to the first N matching items by using the COUNT <count>
	 * option. When ANY is provided the command will return as soon as enough
	 * matches are found, so the results may not be the ones closest to the
	 * specified point, but on the other hand, the effort invested by the server is
	 * significantly lower. When ANY is not provided, the command will perform an
	 * effort that is proportional to the number of items matching the specified
	 * area and sort them, so to query very large areas with a very small COUNT
	 * option may be slow even if just a few results are returned.
	 * 
	 * By default the command returns the items to the client. It is possible to
	 * store the results with one of these options:
	 * 
	 * STORE: Store the items in a sorted set populated with their geospatial
	 * information. STOREDIST: Store the items in a sorted set populated with their
	 * distance from the center as a floating point number, in the same unit
	 * specified in the radius.
	 * 
	 * redis> GEOADD Sicily 13.361389 38.115556 "Palermo" 15.087269 37.502669
	 * "Catania"<br>
	 * (integer) 2<br>
	 * redis> GEORADIUS Sicily 15 37 200 km WITHDIST<br>
	 * 1) 1) "Palermo"<br>
	 * 2) "190.4424"<br>
	 * 2) 1) "Catania"<br>
	 * 2) "56.4413"<br>
	 * redis> GEORADIUS Sicily 15 37 200 km WITHCOORD<br>
	 * 1) 1) "Palermo"<br>
	 * 2) 1) "13.36138933897018433"<br>
	 * 2) "38.11555639549629859"<br>
	 * 2) 1) "Catania"<br>
	 * 2) 1) "15.08726745843887329"<br>
	 * 2) "37.50266842333162032"<br>
	 * redis> GEORADIUS Sicily 15 37 200 km WITHDIST WITHCOORD<br>
	 * 1) 1) "Palermo"<br>
	 * 2) "190.4424"<br>
	 * 3) 1) "13.36138933897018433"<br>
	 * 2) "38.11555639549629859"<br>
	 * 2) 1) "Catania"<br>
	 * 2) "56.4413"<br>
	 * 3) 1) "15.08726745843887329"<br>
	 * 2) "37.50266842333162032"<br>
	 * redis> <br>
	 * 
	 * @param key
	 * @param longitude
	 * @param latitude
	 * @param radius
	 * @param unit
	 * @param param
	 * @param storeParam
	 * @return Array reply, specifically:
	 * 
	 *         Without any WITH option specified, the command just returns a linear
	 *         array like ["New York","Milan","Paris"]. If WITHCOORD, WITHDIST or
	 *         WITHHASH options are specified, the command returns an array of
	 *         arrays, where each sub-array represents a single item. When
	 *         additional information is returned as an array of arrays for each
	 *         item, the first item in the sub-array is always the name of the
	 *         returned item. The other information is returned in the following
	 *         order as successive elements of the sub-array.
	 * 
	 *         The distance from the center as a floating point number, in the same
	 *         unit specified in the radius. The geohash integer. The coordinates as
	 *         a two items x,y array (longitude,latitude). So for example the
	 *         command GEORADIUS Sicily 15 37 200 km WITHCOORD WITHDIST will return
	 *         each item in the following way:<br>
	 * 
	 *         ["Palermo","190.4424",["13.361389338970184","38.115556395496299"]]<br>
	 */
	long georadiusStore(byte[] key, double longitude, double latitude, double radius, GeoUnit unit,
			GeoRadiusStoreArgs<byte[]> storeArgs);

	@NotNull
	List<byte[]> georadiusReadonly(byte[] key, double longitude, double latitude, double radius, GeoUnit unit);

	/**
	 * <h1>查询距离坐标一定距离内成员的地理空间索引</h1><br>
	 * GEORADIUS_RO key longitude latitude radius <M | KM | FT | MI> [WITHCOORD]
	 * [WITHDIST] [WITHHASH] [COUNT count [ANY]] [ASC | DESC]
	 * 
	 * Read-only variant of the GEORADIUS command.
	 * 
	 * This command is identical to the GEORADIUS command, except that it doesn't
	 * support the optional STORE and STOREDIST parameters.
	 * 
	 * @param key
	 * @param longitude
	 * @param latitude
	 * @param radius
	 * @param unit
	 * @param param
	 * @return Array reply: An array with each entry being the corresponding result
	 *         of the subcommand given at the same position.
	 */
	@NotNull
	List<GeoWithin<byte[]>> georadiusReadonly(byte[] key, double longitude, double latitude, double radius,
			GeoUnit unit, GeoArgs args);

	/**
	 * @return members
	 */
	@NotNull
	List<byte[]> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit);

	@NotNull
	List<GeoWithin<byte[]>> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit, GeoArgs args);

	/**
	 * <h1>查询距离某个对象一定距离内成员的地理空间索引，并存储结果（可选）</h1><br>
	 * GEORADIUSBYMEMBER key member radius <M | KM | FT | MI> [WITHCOORD]<br>
	 * [WITHDIST] [WITHHASH] [COUNT count [ANY]] [ASC | DESC] [STORE key]<br>
	 * [STOREDIST key]<br>
	 * 
	 * This command is exactly like GEORADIUS with the sole difference that instead
	 * of taking, as the center of the area to query, a longitude and latitude
	 * value, it takes the name of a member already existing inside the geospatial
	 * index represented by the sorted set.
	 * 
	 * The position of the specified member is used as the center of the query.
	 * 
	 * Please check the example below and the GEORADIUS documentation for more
	 * information about the command and its options.
	 * 
	 * Note that GEORADIUSBYMEMBER_RO is also available since Redis 3.2.10 and Redis
	 * 4.0.0 in order to provide a read-only command that can be used in replicas.
	 * See the GEORADIUS page for more information.
	 * 
	 * redis> GEOADD Sicily 13.583333 37.316667 "Agrigento"<br>
	 * (integer) 1<br>
	 * redis> GEOADD Sicily 13.361389 38.115556 "Palermo" 15.087269 37.502669
	 * "Catania"<br>
	 * (integer) 2<br>
	 * redis> GEORADIUSBYMEMBER Sicily Agrigento 100 km<br>
	 * 1) "Agrigento"<br>
	 * 2) "Palermo"<br>
	 * redis> <br>
	 * 
	 * @param key
	 * @param member
	 * @param radius
	 * @param unit
	 * @param param
	 * @param storeParam
	 * @return
	 */
	long georadiusByMemberStore(byte[] key, byte[] member, double radius, GeoUnit unit,
			GeoRadiusStoreArgs<byte[]> storeArgs);

	@NotNull
	List<byte[]> georadiusByMemberReadonly(byte[] key, byte[] member, double radius, GeoUnit unit);

	/**
	 * <h1>查询距离某个对象一定距离内成员的地理空间索引</h1><br>
	 * GEORADIUSBYMEMBER_RO key member radius <M | KM | FT | MI><br>
	 * [WITHCOORD] [WITHDIST] [WITHHASH] [COUNT count [ANY]] [ASC | DESC]<br>
	 * 
	 * Read-only variant of the GEORADIUSBYMEMBER command.
	 * 
	 * This command is identical to the GEORADIUSBYMEMBER command, except that it
	 * doesn't support the optional STORE and STOREDIST parameters.
	 * 
	 * @param key
	 * @param member
	 * @param radius
	 * @param unit
	 * @param param
	 * @return
	 */
	@NotNull
	List<GeoWithin<byte[]>> georadiusByMemberReadonly(byte[] key, byte[] member, double radius, GeoUnit unit,
			GeoArgs args);

	@NotNull
	List<byte[]> geosearch(byte[] key, GeoSearch.GeoRef<byte[]> reference, GeoSearch.GeoPredicate predicate);

	@NotNull
	List<GeoWithin<byte[]>> geosearch(byte[] key, GeoSearch.GeoRef<byte[]> reference, GeoSearch.GeoPredicate predicate,
			GeoArgs geoArgs);

	long geosearchStore(byte[] destination, byte[] key, GeoSearch.GeoRef<byte[]> reference,
			GeoSearch.GeoPredicate predicate, GeoArgs geoArgs);

	/**
	 * <h1>查询长方体或圆区域内成员的地理空间索引，并存储结果（可选）</h1><br>
	 * GEOSEARCH key <FROMMEMBER member | FROMLONLAT longitude latitude><br>
	 * <BYRADIUS radius <M | KM | FT | MI> | BYBOX width height <M | KM |<br>
	 * FT | MI>> [ASC | DESC] [COUNT count [ANY]] [WITHCOORD] [WITHDIST]<br>
	 * [WITHHASH]<br>
	 * 
	 * Return the members of a sorted set populated with geospatial information
	 * using GEOADD, which are within the borders of the area specified by a given
	 * shape. This command extends the GEORADIUS command, so in addition to
	 * searching within circular areas, it supports searching within rectangular
	 * areas.
	 * 
	 * This command should be used in place of the deprecated GEORADIUS and
	 * GEORADIUSBYMEMBER commands.
	 * 
	 * The query's center point is provided by one of these mandatory options:
	 * 
	 * FROMMEMBER: Use the position of the given existing <member> in the sorted
	 * set. FROMLONLAT: Use the given <longitude> and <latitude> position. The
	 * query's shape is provided by one of these mandatory options:
	 * 
	 * BYRADIUS: Similar to GEORADIUS, search inside circular area according to
	 * given <radius>. BYBOX: Search inside an axis-aligned rectangle, determined by
	 * <height> and <width>. The command optionally returns additional information
	 * using the following options:
	 * 
	 * WITHDIST: Also return the distance of the returned items from the specified
	 * center point. The distance is returned in the same unit as specified for the
	 * radius or height and width arguments. WITHCOORD: Also return the longitude
	 * and latitude of the matching items. WITHHASH: Also return the raw
	 * geohash-encoded sorted set score of the item, in the form of a 52 bit
	 * unsigned integer. This is only useful for low level hacks or debugging and is
	 * otherwise of little interest for the general user. Matching items are
	 * returned unsorted by default. To sort them, use one of the following two
	 * options:
	 * 
	 * ASC: Sort returned items from the nearest to the farthest, relative to the
	 * center point. DESC: Sort returned items from the farthest to the nearest,
	 * relative to the center point. All matching items are returned by default. To
	 * limit the results to the first N matching items, use the COUNT <count>
	 * option. When the ANY option is used, the command returns as soon as enough
	 * matches are found. This means that the results returned may not be the ones
	 * closest to the specified point, but the effort invested by the server to
	 * generate them is significantly less. When ANY is not provided, the command
	 * will perform an effort that is proportional to the number of items matching
	 * the specified area and sort them, so to query very large areas with a very
	 * small COUNT option may be slow even if just a few results are returned.
	 * 
	 * redis> GEOADD Sicily 13.361389 38.115556 "Palermo" 15.087269 37.502669
	 * "Catania"<br>
	 * (integer) 2<br>
	 * redis> GEOADD Sicily 12.758489 38.788135 "edge1" 17.241510 38.788135
	 * "edge2"<br>
	 * (integer) 2<br>
	 * redis> GEOSEARCH Sicily FROMLONLAT 15 37 BYRADIUS 200 km ASC<br>
	 * 1) "Catania"<br>
	 * 2) "Palermo"<br>
	 * redis> GEOSEARCH Sicily FROMLONLAT 15 37 BYBOX 400 400 km ASC WITHCOORD
	 * WITHDIST<br>
	 * 1) 1) "Catania"<br>
	 * 2) "56.4413"<br>
	 * 3) 1) "15.08726745843887329"<br>
	 * 2) "37.50266842333162032"<br>
	 * 2) 1) "Palermo"<br>
	 * 2) "190.4424"<br>
	 * 3) 1) "13.36138933897018433"<br>
	 * 2) "38.11555639549629859"<br>
	 * 3) 1) "edge2"<br>
	 * 2) "279.7403"<br>
	 * 3) 1) "17.24151045083999634"<br>
	 * 2) "38.78813451624225195"<br>
	 * 4) 1) "edge1"<br>
	 * 2) "279.7405"<br>
	 * 3) 1) "12.7584877610206604"<br>
	 * 2) "38.78813451624225195"<br>
	 * redis> <br>
	 * 
	 * @param dest
	 * @param src
	 * @param params
	 * @return Array reply, specifically:
	 * 
	 *         Without any WITH option specified, the command just returns a linear
	 *         array like ["New York","Milan","Paris"]. If WITHCOORD, WITHDIST or
	 *         WITHHASH options are specified, the command returns an array of
	 *         arrays, where each sub-array represents a single item. When
	 *         additional information is returned as an array of arrays for each
	 *         item, the first item in the sub-array is always the name of the
	 *         returned item. The other information is returned in the following
	 *         order as successive elements of the sub-array.
	 * 
	 *         The distance from the center as a floating point number, in the same
	 *         unit specified in the shape. The geohash integer. The coordinates as
	 *         a two items x,y array (longitude,latitude).
	 */
	long geosearchStoreStoreDist(byte[] destination, byte[] key, GeoSearch.GeoRef<byte[]> reference,
			GeoSearch.GeoPredicate predicate, GeoArgs geoArgs);
}
