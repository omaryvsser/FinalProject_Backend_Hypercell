-- [ADDED FOR TESTING] - Initial Dummy Data for H2 Test Setup
-- =========================================================

-- 1. Insert Dummy Users
INSERT INTO users (id, name, email, role) VALUES (1, 'Kareem Organizer', 'organizer@test.com', 'ORGANIZER');
INSERT INTO users (id, name, email, role) VALUES (2, 'Ahmed Ali', 'ahmed@test.com', 'USER');
INSERT INTO users (id, name, email, role) VALUES (3, 'Sara Hassan', 'sara@test.com', 'USER');

-- 2. Insert Dummy Movie
INSERT INTO movies (id, title, duration) VALUES (3, 'Inception', 148);

-- 3. Insert Dummy Bookings for Movie ID = 3
INSERT INTO bookings (id, movie_id, user_id, seats_booked, booking_time) 
VALUES (101, 3, 2, 2, CURRENT_TIMESTAMP());

INSERT INTO bookings (id, movie_id, user_id, seats_booked, booking_time) 
VALUES (102, 3, 3, 3, CURRENT_TIMESTAMP());

-- =========================================================