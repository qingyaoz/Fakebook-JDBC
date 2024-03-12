package project2;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;

/*
    The StudentFakebookOracle class is derived from the FakebookOracle class and implements
    the abstract query functions that investigate the database provided via the <connection>
    parameter of the constructor to discover specific information.
*/
public final class StudentFakebookOracle extends FakebookOracle {
    // [Constructor]
    // REQUIRES: <connection> is a valid JDBC connection
    public StudentFakebookOracle(Connection connection) {
        oracle = connection;
    }

    @Override
    // Query 0
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the total number of users for which a birth month is listed
    //        (B) Find the birth month in which the most users were born
    //        (C) Find the birth month in which the fewest users (at least one) were born
    //        (D) Find the IDs, first names, and last names of users born in the month
    //            identified in (B)
    //        (E) Find the IDs, first names, and last name of users born in the month
    //            identified in (C)
    //
    // This query is provided to you completed for reference. Below you will find the appropriate
    // mechanisms for opening up a statement, executing a query, walking through results, extracting
    // data, and more things that you will need to do for the remaining nine queries
    public BirthMonthInfo findMonthOfBirthInfo() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            // Step 1
            // ------------
            // * Find the total number of users with birth month info
            // * Find the month in which the most users were born
            // * Find the month in which the fewest (but at least 1) users were born
            ResultSet rst = stmt.executeQuery(
                    "SELECT COUNT(*) AS Birthed, Month_of_Birth " + // select birth months and number of uses with that birth month
                            "FROM " + UsersTable + " " + // from all users
                            "WHERE Month_of_Birth IS NOT NULL " + // for which a birth month is available
                            "GROUP BY Month_of_Birth " + // group into buckets by birth month
                            "ORDER BY Birthed DESC, Month_of_Birth ASC"); // sort by users born in that month, descending; break ties by birth month

            int mostMonth = 0;
            int leastMonth = 0;
            int total = 0;
            while (rst.next()) { // step through result rows/records one by one
                if (rst.isFirst()) { // if first record
                    mostMonth = rst.getInt(2); //   it is the month with the most
                }
                if (rst.isLast()) { // if last record
                    leastMonth = rst.getInt(2); //   it is the month with the least
                }
                total += rst.getInt(1); // get the first field's value as an integer
            }
            BirthMonthInfo info = new BirthMonthInfo(total, mostMonth, leastMonth);

            // Step 2
            // ------------
            // * Get the names of users born in the most popular birth month
            rst = stmt.executeQuery(
                    "SELECT User_ID, First_Name, Last_Name " + // select ID, first name, and last name
                            "FROM " + UsersTable + " " + // from all users
                            "WHERE Month_of_Birth = " + mostMonth + " " + // born in the most popular birth month
                            "ORDER BY User_ID"); // sort smaller IDs first

            while (rst.next()) {
                info.addMostPopularBirthMonthUser(new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)));
            }

            // Step 3
            // ------------
            // * Get the names of users born in the least popular birth month
            rst = stmt.executeQuery(
                    "SELECT User_ID, First_Name, Last_Name " + // select ID, first name, and last name
                            "FROM " + UsersTable + " " + // from all users
                            "WHERE Month_of_Birth = " + leastMonth + " " + // born in the least popular birth month
                            "ORDER BY User_ID"); // sort smaller IDs first

            while (rst.next()) {
                info.addLeastPopularBirthMonthUser(new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)));
            }

            // Step 4
            // ------------
            // * Close resources being used
            rst.close();
            stmt.close(); // if you close the statement first, the result set gets closed automatically

            return info;

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new BirthMonthInfo(-1, -1, -1);
        }
    }

    
    @Override
    // Query 1
    // -----------------------------------------------------------------------------------
    // GOALS: (A) The first name(s) with the most letters
    //        (B) The first name(s) with the fewest letters
    //        (C) The first name held by the most users
    //        (D) The number of users whose first name is that identified in (C)
    public FirstNameInfo findNameInfo() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                FirstNameInfo info = new FirstNameInfo();
                info.addLongName("Aristophanes");
                info.addLongName("Michelangelo");
                info.addLongName("Peisistratos");
                info.addShortName("Bob");
                info.addShortName("Sue");
                info.addCommonName("Harold");
                info.addCommonName("Jessica");
                info.setCommonNameCount(42);
                return info;
            */
            int max_length = 0;
            int min_length = 0;
            FirstNameInfo info = new FirstNameInfo();
            // A
            // ResultSet rst = stmt.executeQuery(
            //     "SELECT First_Name " +
            //     "FROM " + UsersTable + " " +
            //     "GROUP BY First_Name " +
            //     "HAVING LENGTH(First_Name) = (SELECT MAX(LENGTH(First_Name)) FROM " + UsersTable + ")"
            // );
            // while (rst.next()) {
            //     System.out.println("debug1");
            //     info.addLongName(rst.getString(1));
            //     System.out.println(rst.getString(1));
            // }

            ResultSet rst = stmt.executeQuery(
                    "SELECT DISTINCT LENGTH(First_Name) AS Length, First_Name " + 
                    "FROM " + UsersTable + " " +
                    "ORDER BY Length DESC, First_Name ASC");

            while (rst.next()) {
                if (rst.isFirst()) { // if first record
                    max_length = rst.getInt(1);
                    info.addLongName(rst.getString(2));
                }
                else if (rst.getInt(1) == max_length) {
                    info.addLongName(rst.getString(2));
                }
            }
            // B
            rst = stmt.executeQuery(
                "SELECT DISTINCT LENGTH(First_Name) AS Length, First_Name " + 
                "FROM " + UsersTable + " " +
                "ORDER BY Length ASC, First_Name ASC"
            );

            while (rst.next()) {
                if (rst.isFirst()) { // if first record
                    min_length = rst.getInt(1);
                    info.addShortName(rst.getString(2));
                }
                else if (rst.getInt(1) == min_length) {
                    info.addShortName(rst.getString(2));
                }
            }

            // C
            rst  = stmt.executeQuery(
                "SELECT COUNT(*) AS Com_Count, First_Name " +
                "FROM " + UsersTable + " " +
                "GROUP BY First_Name " +
                "ORDER BY Com_Count DESC, First_Name ASC"
            );
            int count = 0;
            while (rst.next()) {
                if (rst.isFirst()) {
                    count = rst.getInt(1);
                    info.setCommonNameCount(count);
                }
                if (rst.getInt(1) == count) {
                    info.addCommonName(rst.getString(2));
                }
            }
            rst.close();
            stmt.close(); 
            return info; // placeholder for compilation
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new FirstNameInfo();
        }
    }

    @Override
    // Query 2
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of users without any friends
    //
    // Be careful! Remember that if two users are friends, the Friends table only contains
    // the one entry (U1, U2) where U1 < U2.
    public FakebookArrayList<UserInfo> lonelyUsers() throws SQLException {
        FakebookArrayList<UserInfo> results = new FakebookArrayList<UserInfo>(", ");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(15, "Abraham", "Lincoln");
                UserInfo u2 = new UserInfo(39, "Margaret", "Thatcher");
                results.add(u1);
                results.add(u2);
            */
            ResultSet rst = stmt.executeQuery(
                "SELECT U.User_ID, U.First_Name, U.Last_Name " +
                "FROM " + UsersTable + " U " +
                "WHERE U.User_ID NOT IN (" + 
                    "SELECT F1.User1_ID " +
                    "FROM " + FriendsTable + " F1 " +
                    "UNION " +
                    "SELECT F2.User2_ID " +
                    "FROM " + FriendsTable + " F2" + 
                ")" + 
                "ORDER by U.User_ID ASC"
            );

            while (rst.next()) {
                results.add(new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)));
            }

            rst.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 3
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of users who no longer live
    //            in their hometown (i.e. their current city and their hometown are different)
    public FakebookArrayList<UserInfo> liveAwayFromHome() throws SQLException {
        FakebookArrayList<UserInfo> results = new FakebookArrayList<UserInfo>(", ");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(9, "Meryl", "Streep");
                UserInfo u2 = new UserInfo(104, "Tom", "Hanks");
                results.add(u1);
                results.add(u2);
            */
            ResultSet rst = stmt.executeQuery(
                "SELECT U.User_ID, U.First_Name, U.Last_Name " +
                "FROM " + UsersTable + " U " +
                "JOIN " + HometownCitiesTable + " H ON H.User_ID = U.User_ID " +
                "JOIN " + CurrentCitiesTable + " C ON C.User_ID = U.User_ID " +
                "WHERE H.Hometown_City_ID <> C.Current_City_ID " +
                "ORDER by U.User_ID ASC"
            );

            while (rst.next()) {
                results.add(new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)));
            }

            rst.close();
            stmt.close();
            
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 4
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, links, and IDs and names of the containing album of the top
    //            <num> photos with the most tagged users
    //        (B) For each photo identified in (A), find the IDs, first names, and last names
    //            of the users therein tagged
    public FakebookArrayList<TaggedPhotoInfo> findPhotosWithMostTags(int num) throws SQLException {
        FakebookArrayList<TaggedPhotoInfo> results = new FakebookArrayList<TaggedPhotoInfo>("\n");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                PhotoInfo p = new PhotoInfo(80, 5, "www.photolink.net", "Winterfell S1");
                UserInfo u1 = new UserInfo(3901, "Jon", "Snow");
                UserInfo u2 = new UserInfo(3902, "Arya", "Stark");
                UserInfo u3 = new UserInfo(3903, "Sansa", "Stark");
                TaggedPhotoInfo tp = new TaggedPhotoInfo(p);
                tp.addTaggedUser(u1);
                tp.addTaggedUser(u2);
                tp.addTaggedUser(u3);
                results.add(tp);
            */
            ResultSet rst = stmt.executeQuery(
                "SELECT P.photo_id, P.album_id, P.photo_link, A.album_name, COUNT(T.tag_subject_id) AS tag_count " +
                "FROM " + PhotosTable + " P " +
                "JOIN " + AlbumsTable + " A ON P.album_id = A.album_id " +
                "JOIN " + TagsTable + " T ON P.photo_id = T.tag_photo_id " + 
                "GROUP BY P.photo_id, P.album_id, P.photo_link, A.album_name " +
                "ORDER BY tag_count DESC, P.photo_id ASC"
            );

            int count = 0;
            while (rst.next()) {
                if (count == num) {
                    break;
                }
                count++;
                
                PhotoInfo p = new PhotoInfo(rst.getLong(1), rst.getLong(2), rst.getString(3), rst.getString(4));
                TaggedPhotoInfo tp = new TaggedPhotoInfo(p);

                // B
                try (Statement stmt2 = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
                    ResultSet rst2 = stmt2.executeQuery(
                    "SELECT U.user_id, U.first_name, U.last_name " +
                    "FROM " + UsersTable + " U " + 
                    "JOIN " + TagsTable + " T ON T.tag_subject_id = U.user_id " +
                    "WHERE T.tag_photo_id = " + rst.getLong(1) + " " +
                    "ORDER BY U.user_id ASC"
                    );

                    while (rst2.next()) {
                        tp.addTaggedUser(new UserInfo(rst2.getLong(1), rst2.getString(2), rst2.getString(3)));
                    }
                    results.add(tp);
                    rst2.close();
                    stmt2.close();
                }
            }
            
            rst.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 5
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, last names, and birth years of each of the two
    //            users in the top <num> pairs of users that meet each of the following
    //            criteria:
    //              (i) same gender
    //              (ii) tagged in at least one common photo
    //              (iii) difference in birth years is no more than <yearDiff>
    //              (iv) not friends
    //        (B) For each pair identified in (A), find the IDs, links, and IDs and names of
    //            the containing album of each photo in which they are tagged together
    public FakebookArrayList<MatchPair> matchMaker(int num, int yearDiff) throws SQLException {
        FakebookArrayList<MatchPair> results = new FakebookArrayList<MatchPair>("\n");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(93103, "Romeo", "Montague");
                UserInfo u2 = new UserInfo(93113, "Juliet", "Capulet");
                MatchPair mp = new MatchPair(u1, 1597, u2, 1597);
                PhotoInfo p = new PhotoInfo(167, 309, "www.photolink.net", "Tragedy");
                mp.addSharedPhoto(p);
                results.add(mp);
            */
            // Step 1
            // ------------
            // Find all pairs of users who have been tagged in the same photos
            stmt.executeUpdate(
                "CREATE VIEW CommonTagged AS " +
                "SELECT T1.Tag_Subject_Id AS User1_Id, T2.Tag_Subject_Id AS User2_Id, P.Photo_Id AS CT_Photo_Id, " +
                "P.Photo_link AS CT_Photo_link, A.Album_Id AS CT_Album_Id, A.Album_Name AS CT_Album_Name " +
                "FROM " + TagsTable + " T1 " +
                "JOIN " + TagsTable + " T2 ON T1.Tag_Photo_Id = T2.Tag_Photo_Id AND T1.Tag_Subject_Id < T2.Tag_Subject_Id " +
                "JOIN " + PhotosTable + " P ON T1.Tag_Photo_Id = P.Photo_Id " +
                "JOIN " + AlbumsTable + " A ON P.Album_Id = A.Album_Id"
            );
            
            // Find all pairs of users who are not friends yet
            stmt.executeUpdate(
                "CREATE VIEW NotFriends AS " +
                "SELECT U1.User_Id AS User1_Id, U2.User_Id AS User2_Id " +
                "FROM " + UsersTable + " U1 " +
                "JOIN " + UsersTable + " U2 ON U1.User_Id < U2.User_Id " +
                "WHERE NOT EXISTS (" + 
                    "SELECT 1 FROM " + FriendsTable + " f " + 
                    "WHERE (f.user1_id = U1.User_Id AND f.user2_id = U2.User_Id) " +
                    "OR (f.user1_id = U2.User_Id AND f.user2_id = U1.User_Id)" +
                ")"
            );

            // Find all pairs of users who have same gender and meet the age difference requirement
            stmt.executeUpdate(
                "CREATE VIEW PotentialFriends AS " +
                "SELECT U1.User_Id AS User1_Id, U1.First_Name AS User1_First, U1.Last_Name AS User1_Last, " +
                "U1.Year_of_Birth AS User1_Year, U2.User_Id AS User2_Id, U2.First_Name AS User2_First, " +
                "U2.Last_Name AS User2_Last, U2.Year_of_Birth AS User2_Year, " +
                "COUNT(CT.CT_Photo_Id) AS Common_Count " +
                "FROM " + UsersTable + " U1 " +
                "JOIN " + UsersTable + " U2 ON U1.User_Id < U2.User_Id " +
                "JOIN NotFriends N ON U1.User_Id =  N.User1_Id AND U2.User_Id = N.User2_Id AND " + 
                "U1.Gender = U2.Gender AND ABS(U1.Year_of_Birth -U2.Year_of_Birth) <= " + yearDiff +
                "JOIN CommonTagged CT ON U1.User_Id = CT.User1_Id AND U2.User_Id = CT.User2_Id " +
                "GROUP BY U1.User_Id, U1.First_Name, U1.Last_Name, U1.Year_of_Birth, " +
                "U2.User_Id, U2.First_Name, U2.Last_Name, U2.Year_of_Birth " +
                "ORDER BY COUNT(CT.CT_Photo_Id) DESC, U1.User_Id ASC, U2.User_Id ASC"
            );

            // Step 2
            // ------------
            // Map tables to select all necessary columns
            ResultSet rst = stmt.executeQuery(
                "SELECT PF.User1_Id, PF.User1_First, PF.User1_Last, PF.User1_Year, PF.User2_Id, PF.User2_First, " + 
                "PF.User2_Last, PF.User2_Year, CT.CT_Photo_Id, CT.CT_Album_Id, CT.CT_Photo_link, CT.CT_Album_Name " +
                "FROM PotentialFriends PF " +
                "JOIN CommonTagged CT ON PF.User1_Id = CT.User1_Id AND PF.User2_Id = CT.User2_Id " +
                "ORDER BY Common_Count DESC, PF.User1_Id ASC, PF.User2_Id ASC, CT.CT_Photo_ID ASC"
            );

            int count = 0;
            long ptF1_id = 0;
            long ptF2_id = 0;
            MatchPair currPtF = null;
            while (rst.next()) {
                if (count > num) {break;}
                if (ptF1_id != rst.getLong(1) && ptF2_id != rst.getLong(5)){
                    UserInfo ptF1 = new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)); // ID + name
                    UserInfo ptF2 = new UserInfo(rst.getLong(5), rst.getString(6), rst.getString(7)); // ID + name
                    MatchPair ptFriend = new MatchPair(ptF1, rst.getLong(4), ptF2, rst.getLong(8)); // User + Year
                    // photoID, albumID, photoLink, albumName
                    ptFriend.addSharedPhoto(new PhotoInfo(rst.getLong(9), rst.getLong(10), rst.getString(11), rst.getString(12)));
                    results.add(ptFriend);
                    count += 1;

                    ptF1_id = rst.getLong(1);
                    ptF2_id = rst.getLong(5);
                    currPtF = ptFriend;
                } else {
                    currPtF.addSharedPhoto(new PhotoInfo(rst.getLong(9), rst.getLong(10), rst.getString(11), rst.getString(12)));
                }
            }
            
            rst.close();
            stmt.executeUpdate("DROP VIEW CommonTagged");
            stmt.executeUpdate("DROP VIEW NotFriends");
            stmt.executeUpdate("DROP VIEW PotentialFriends");
            stmt.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 6
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of each of the two users in
    //            the top <num> pairs of users who are not friends but have a lot of
    //            common friends
    //        (B) For each pair identified in (A), find the IDs, first names, and last names
    //            of all the two users' common friends
    public FakebookArrayList<UsersPair> suggestFriends(int num) throws SQLException {
        FakebookArrayList<UsersPair> results = new FakebookArrayList<UsersPair>("\n");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(16, "The", "Hacker");
                UserInfo u2 = new UserInfo(80, "Dr.", "Marbles");
                UserInfo u3 = new UserInfo(192, "Digit", "Le Boid");
                UsersPair up = new UsersPair(u1, u2);
                up.addSharedFriend(u3);
                results.add(up);
            */
            // stmt.executeUpdate("DROP VIEW BidirectionalFriends"); // debug

            stmt.executeUpdate(
                "CREATE OR REPLACE VIEW BidirectionalFriends AS " +
                "SELECT user1_id, user2_id " +
                "FROM " + FriendsTable + " " +
                "UNION " +
                "SELECT user2_id, user1_id " + 
                "FROM " + FriendsTable + " " +
                "ORDER BY user1_id ASC, user2_id ASC "
            );

            stmt.executeUpdate(
                "CREATE OR REPLACE VIEW NonFriends AS " +
                "SELECT U1.user_id AS User1ID, U2.user_id AS User2ID " +
                "FROM " + UsersTable + " U1, " + UsersTable + " U2 " +
                "WHERE U1.user_id < U2.user_id " +
                "MINUS " +
                "SELECT user1_id, user2_id FROM BidirectionalFriends "
            );

            ResultSet rst = stmt.executeQuery(
                "SELECT NF.User1ID, U1.first_name AS user1_first_name, U1.last_name AS user1_last_name, " +
                "NF.User2ID, U2.first_name AS user2_first_name, U2.last_name AS user2_last_name, COUNT(*) AS Num_Mutual " +
                "FROM NonFriends NF " +
                "JOIN BidirectionalFriends B1 ON B1.user2_id = NF.User1ID " +
                "JOIN BidirectionalFriends B2 ON B2.user2_id = NF.User2ID AND B1.user1_id = B2.user1_id " +
                "JOIN " + UsersTable + " U1 ON NF.User1ID = U1.user_id " +
                "JOIN " + UsersTable + " U2 ON NF.User2ID = U2.user_id " +
                "GROUP BY NF.User1ID, U1.first_name, U1.last_name, NF.User2ID, U2.first_name, U2.last_name " +
                "HAVING COUNT(*) > 0 " +
                "ORDER BY Num_Mutual DESC, NF.User1ID ASC, NF.User2ID ASC "
            );

            int count = 0;
            while (rst.next()) {
                if (count == num) {
                    break;
                }
                count++;
                Long user1Id = rst.getLong(1);
                Long user2Id = rst.getLong(4);
                UserInfo u1 = new UserInfo(user1Id, rst.getString(2), rst.getString(3));
                UserInfo u2 = new UserInfo(user2Id, rst.getString(5), rst.getString(6));
                UsersPair up = new UsersPair(u1, u2);
                
                // B
                 try (Statement innerStmt = oracle.createStatement()) {
                    ResultSet rst2 = innerStmt.executeQuery(
                        "SELECT B1.user2_id, U.First_Name, U.Last_Name " +
                        "FROM " + UsersTable + " U " +
                        "JOIN BidirectionalFriends B1 ON U.user_id = B1.user2_id " +
                        "JOIN BidirectionalFriends B2 ON B1.user2_id = B2.user2_id " +
                        "WHERE B1.user1_id = " + user1Id + " AND B2.user1_id = " + user2Id + " " +
                        "ORDER BY B1.user2_id ASC"
                    );
                    while (rst2.next()) {
                        UserInfo u3 = new UserInfo(rst2.getLong(1), rst2.getString(2), rst2.getString(3));
                        up.addSharedFriend(u3);
                    }
                }
                results.add(up);
            }
            
            rst.close();
            stmt.executeUpdate("DROP VIEW NonFriends");
            stmt.executeUpdate("DROP VIEW BidirectionalFriends");
            stmt.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 7
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the name of the state or states in which the most events are held
    //        (B) Find the number of events held in the states identified in (A)
    public EventStateInfo findEventStates() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                EventStateInfo info = new EventStateInfo(50);
                info.addState("Kentucky");
                info.addState("Hawaii");
                info.addState("New Hampshire");
                return info;
            */
            ResultSet rst = stmt.executeQuery(
                "SELECT C.state_name, COUNT(*) AS Event_Num " + 
                "FROM " + EventsTable + " E " +
                "JOIN " + CitiesTable + " C ON E.event_city_id = C.city_id " +
                "GROUP BY C.state_name " +
                "ORDER BY Event_Num DESC, C.state_name ASC"
            );

            Long maxCount = null;
            EventStateInfo info = null;
            while (rst.next()) {
                if (rst.isFirst()) {
                    info = new EventStateInfo(rst.getLong(2));
                    info.addState(rst.getString(1));
                    maxCount = rst.getLong(2);
                } else if (rst.getLong(2) != maxCount) {
                    break;
                } else {
                    info.addState(rst.getString(1));
                }
            }

            rst.close();
            stmt.close();

            return info; // placeholder for compilation
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new EventStateInfo(-1);
        }
    }

    @Override
    // Query 8
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the ID, first name, and last name of the oldest friend of the user
    //            with User ID <userID>
    //        (B) Find the ID, first name, and last name of the youngest friend of the user
    //            with User ID <userID>
    public AgeInfo findAgeInfo(long userID) throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo old = new UserInfo(12000000, "Galileo", "Galilei");
                UserInfo young = new UserInfo(80000000, "Neil", "deGrasse Tyson");
                return new AgeInfo(old, young);
            */
            // Step 1
            // ------------
            // Create a friends table of specific user: user  friends' infor...
            stmt.executeUpdate(
                "CREATE VIEW UserFriends AS " +
                "SELECT F.friends_id, U.first_name, U.last_name, U.year_of_birth, " +
                "U.month_of_birth, U.day_of_birth " +
                "FROM (SELECT user1_id AS user_id, user2_id AS friends_id " +
                    "FROM " + FriendsTable + " " +
                    "WHERE user1_id = " + userID + " " +
                    "UNION " +
                    "SELECT user2_id, user1_id " + 
                    "FROM " + FriendsTable + " " +
                    "WHERE user2_id = " + userID + ") F " +
                "JOIN " + UsersTable + " U ON F.friends_id = U.user_id");

            ResultSet rst = stmt.executeQuery(
                "SELECT friends_id, first_name, last_name " +
                "FROM UserFriends " +
                "ORDER BY year_of_birth ASC, month_of_birth ASC, " + 
                "day_of_birth ASC, friends_id DESC");

            rst.next();
            UserInfo oldestFriend = new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3));

            rst = stmt.executeQuery(
                "SELECT friends_id, first_name, last_name " +
                "FROM UserFriends " +
                "ORDER BY year_of_birth DESC, month_of_birth DESC, " + 
                "day_of_birth DESC, friends_id DESC");

            rst.next();
            UserInfo youngestFriend = new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3));

            AgeInfo info = new AgeInfo(oldestFriend, youngestFriend);

            rst.close();
            stmt.executeUpdate("DROP VIEW UserFriends");
            stmt.close();

            return info;

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new AgeInfo(new UserInfo(-1, "ERROR", "ERROR"), new UserInfo(-1, "ERROR", "ERROR"));
        }
    }

    @Override
    // Query 9
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find all pairs of users that meet each of the following criteria
    //              (i) same last name
    //              (ii) same hometown
    //              (iii) are friends
    //              (iv) less than 10 birth years apart
    public FakebookArrayList<SiblingInfo> findPotentialSiblings() throws SQLException {
        FakebookArrayList<SiblingInfo> results = new FakebookArrayList<SiblingInfo>("\n");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(81023, "Kim", "Kardashian");
                UserInfo u2 = new UserInfo(17231, "Kourtney", "Kardashian");
                SiblingInfo si = new SiblingInfo(u1, u2);
                results.add(si);
            */
            // Step 1
            // ------------
            // Create a new friends table with smaller user_id as the first column 
            stmt.executeUpdate(
                "CREATE VIEW OrderedFriends AS " +
                "SELECT LEAST(user1_id, user2_id) AS younger_id, GREATEST(user1_id, user2_id) AS older_id " +
                "FROM " + FriendsTable + " F"
            );

            // Step 2
            // ------------
            // Find all pairs of users who have same gender and meet the age difference requirement
            ResultSet rst = stmt.executeQuery(
                "SELECT F.younger_id, U1.first_name, U1.last_name, F.older_id, U2.first_name, U2.last_name " +
                "FROM OrderedFriends F " + // The two users are friends
                "JOIN " + UsersTable + " U1 ON F.younger_id = U1.user_id " +
                "JOIN " + HometownCitiesTable + " H1 ON U1.user_id = H1.user_id " +
                "JOIN " + UsersTable + " U2 ON F.older_id = U2.user_id " +
                "JOIN " + HometownCitiesTable + " H2 ON U2.user_id = H2.user_id " +
                "WHERE U1.last_name = U2.last_name AND H1.hometown_city_id = H2.hometown_city_id " +
                "AND ABS(U1.year_of_birth - U2.year_of_birth) < 10 " +
                "ORDER BY F.younger_id ASC, F.older_id ASC");

            while (rst.next()) {
                UserInfo user1 = new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3));
                UserInfo user2 = new UserInfo(rst.getLong(4), rst.getString(5), rst.getString(6));
                results.add(new SiblingInfo(user1, user2));
            }
            
            rst.close();
            stmt.executeUpdate("DROP VIEW OrderedFriends");
            stmt.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    // Member Variables
    private Connection oracle;
    private final String UsersTable = FakebookOracleConstants.UsersTable;
    private final String CitiesTable = FakebookOracleConstants.CitiesTable;
    private final String FriendsTable = FakebookOracleConstants.FriendsTable;
    private final String CurrentCitiesTable = FakebookOracleConstants.CurrentCitiesTable;
    private final String HometownCitiesTable = FakebookOracleConstants.HometownCitiesTable;
    private final String ProgramsTable = FakebookOracleConstants.ProgramsTable;
    private final String EducationTable = FakebookOracleConstants.EducationTable;
    private final String EventsTable = FakebookOracleConstants.EventsTable;
    private final String AlbumsTable = FakebookOracleConstants.AlbumsTable;
    private final String PhotosTable = FakebookOracleConstants.PhotosTable;
    private final String TagsTable = FakebookOracleConstants.TagsTable;
}
