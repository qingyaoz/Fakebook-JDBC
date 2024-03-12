# Fakebook-JDBC
Building a Java application that executes SQL queries against a relational database and places the results in special data structures.

## Objective
The goal of this project is to have additional practice with standard SQL query practices in addition to hands-on experience with real-world database application programming.

## Database Scheme
Refer to https://eecs484db.github.io/wn24/p2-fakebook-jdbc.

## Queries
### Query 0: Birth Months
Query 0 asks to identify information about Fakebook users’ birth months.

It should determine in which month the most Fakebook users were born and in which month the fewest (but at least 1) Fakebook users were born. If there are ties, pick the month that occurs earliest in the calendar year. For each of those months, report the IDs, first names, and last names of the Fakebook users born in that month; sort the users in ascending order by ID. It should also report the total number of Fakebook users that have a birth month listed. Assume that at least one Fakebook user has listed a birth month.

### Query 1: First Names

Query 1 asks to identify information about Fakebook users’ first names.

Fakebook would like to know the longest and shortest first names by length. If there are ties between multiple names, report all tied names in alphabetical order.
Fakebook would also like to know what first name(s) are the most common and how many users have that first name. If there are ties, report all tied names in alphabetical order.
Hint: Consider using the LENGTH() operation in SQL.

### Query 2: Lonely Users

Query 2 asks to identify all of the Fakebook users with no Fakebook friends. For each user without any friends, report their ID, first name, and last name. The users should be reported in ascending order by ID. If every Fakebook user has at least one Fakebook friend, it should return an empty FakebookArrayList.

### Query 3: Users Who Live Away From Home

Query 3 asks to identify all of the Fakebook users that no longer live in their hometown. For each such user, report their ID, first name, and last name. Results should be sorted in ascending order by the users’ ID. If a user does not have a current city or a hometown listed, they should not be included in the results. If every Fakebook user still lives in his/her hometown, it should return an empty FakebookArrayList.

### Query 4: Highly-Tagged Photos

Query 4 asks to identify the most highly-tagged photos. We will pass an integer argument num to the query function; it should return the top num photos with the most tagged users sorted in descending order by the number of tagged users (most tagged users first). If there are fewer than num photos with at least 1 tag, then it should return only those available photos. If more than one photo has the same number of tagged users, list the photo with the smaller ID first.

For each photo, it should report the photo’s ID, the ID of the album containing the photo, the photo’s Fakebook link, and the name of the album containing the photo. For each reported photo, it should list the ID, first name, and last name of the users tagged in that photo. Tagged users should be listed in ascending order by ID.

Query 5: Matchmaker

Query 5 asks to suggest possible unrealized Fakebook friendships. We will pass two integer arguments, num and yearDiff to the query function; it should return the top num pairs of two Fakebook users who meet each of the following conditions:

* The two users are the same gender
* The two users are tagged in at least one common photo
* The two users are not friends
* The difference in the two users’ birth years is less than or equal to yearDiff
  
The pairs of users should be reported in (and cut-off based on) descending order by the number of photos in which the two users were tagged together. For each pair, report the IDs, first names, and last names of the two users; list the user with the smaller ID first. If multiple pairs of users that meet the criteria are tagged in the same number of photos, order the results in ascending order by the smaller user ID and then in ascending order by the larger user ID. If there are fewer than num pairs of users that meet the criteria, it should return only those pairs that are viable.

For each pair of users, it should also report the photos in which they were tagged together. The information it should report is the photo’s ID, the photo’s Fakebook link, the ID of the album containing the photo, and the name of the album containing the photo. List the photos in ascending order by photo ID.

### Query 6: Suggest Friends

Query 6 asks  to suggest possible unrealized Fakebook friendships in a different way. We will pass a single integer argument, num, to the query function; it should return the top num pairs of Fakebook users with the most mutual friends who are not friends themselves. If there are fewer than num pairs, then it should return only those available pairs.

A mutual friend is one such that A is friends with B and B is friends with C, in which case B is a mutual friend of A and C. The IDs, first names, and last names of the two users who share a mutual friend should be returned; list the user with the smaller ID first and larger ID second within the pair and rank the pairs in descending order by the number of mutual friends. In the event of a tie between pairs, list the pair with the smaller first ID before the pair with the larger first ID; if pairs are still tied, list the pair with the smaller second ID before the pair with the larger second ID.

For each pair of users it report, it should also list the IDs, first names, and last names of all their mutual friends. List the mutual friends in ascending order by ID.

Remark: The friends table contains one direction of user IDs for each friendship.

### Query 7: Event-Heavy States

Query 7 asks to identify the states in which the most Fakebook events are held. If more than one state is tied for hosting the most Fakebook events, all states involved in the tie should be returned, listed in ascending order by state name. It also need to report how many events are held in those state(s). Assume that there is at least 1 Fakebook event.

### Query 8: Oldest and Youngest Friends

Query 8 asks  to identify the oldest and youngest friend of a particular Fakebook user. We will pass a single integer argument, userID, to the query function; it should return the ID, first name, and last name of the oldest and youngest friend of the Fakebook user with that ID. Notice that it should not type convert the date, month and year fields using TO_DATE; instead, order them just as they are (numbers). If two friends of the user passed as the argument are born on the exact same date, report the one with the larger user ID. Assume that the user with the specified ID has at least 1 Fakebook friend.

### Query 9: Potential Siblings

Query 9 asks to identify pairs of Fakebook users that might be siblings. Two users might be siblings if they meet each of the following criteria:

* The two users have the same last name
* The two users have the same hometown
* The two users are friends
* The difference in the two users’ birth years is strictly less than 10 years

Each pair should be reported with the smaller user ID first and the larger user ID second. The smaller ID should be used to order pairs relative to one another (smaller smaller ID first); the larger ID should be used to break ties (smaller larger ID first).
