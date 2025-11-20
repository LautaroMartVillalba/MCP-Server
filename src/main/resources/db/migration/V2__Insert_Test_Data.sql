-- V2__Insert_Test_Data.sql
-- Insert States (Argentina provinces)
INSERT INTO entity_states (code, country_code, subdivision_name) VALUES
('AR-B', 'AR', 'Buenos Aires'),
('AR-C', 'AR', 'Ciudad Autónoma de Buenos Aires'),
('AR-S', 'AR', 'Santa Fe'),
('AR-X', 'AR', 'Córdoba'),
('AR-M', 'AR', 'Mendoza'),
('AR-A', 'AR', 'Salta'),
('AR-R', 'AR', 'Río Negro'),
('AR-K', 'AR', 'Catamarca');

-- Insert Hotels (5 hotels with varying star ratings)
INSERT INTO entity_hotel (name, contact_phone, stars, total_rooms, free_rooms, reserved_rooms) VALUES
('Luxury Palace Hotel', '5411-4000100', 5.0, 50, 42, 8),
('Executive Business Hotel', '5411-4100200', 4.0, 80, 65, 15),
('Comfort Inn Express', '5411-4200300', 3.5, 120, 95, 25),
('Budget Plaza Hotel', '5411-4300400', 2.5, 100, 78, 22),
('Eco Resort Mountain', '549-264450010', 4.5, 40, 35, 5);

-- Insert Hotel Addresses
INSERT INTO entity_address (street, number, floor, door_number, state_code, address_type, entity_hotel_id) VALUES
('Avenida 9 de Julio', '1234', NULL, NULL, 'AR-C', 'HOTEL', 1),
('Calle Florida', '567', '10', 'A', 'AR-C', 'HOTEL', 2),
('Avenida Corrientes', '3000', NULL, NULL, 'AR-C', 'HOTEL', 3),
('Calle Rivadavia', '2500', '5', 'B', 'AR-B', 'HOTEL', 4),
('Ruta Nacional 7', '450', NULL, NULL, 'AR-R', 'HOTEL', 5);

-- Insert Benefits (services/amenities)
INSERT INTO entity_benefit (name, description, open_at, close_at) VALUES
('Swimming Pool', 'Olympic-sized indoor and outdoor swimming pools with heated water, diving boards, and comfortable lounge chairs for relaxation', '06:00:00', '22:00:00'),
('Spa & Wellness', 'Full-service spa offering massages, facials, aromatherapy, body treatments, and wellness programs by certified therapists', '09:00:00', '21:00:00'),
('Fitness Center', 'State-of-the-art gym with cardio equipment, weight machines, free weights, and personal training sessions available', '05:00:00', '23:00:00'),
('Restaurant', 'Gourmet on-site restaurant featuring international cuisine, local specialties, buffet breakfast, and à la carte dinner', '07:00:00', '23:00:00'),
('Bar & Lounge', 'Elegant bar with premium cocktails, wines, craft beers, live music on weekends, and sophisticated entertainment', '18:00:00', '02:00:00'),
('Free WiFi', 'High-speed wireless internet connection throughout all hotel areas including rooms, lobby, restaurant, and pool', '00:00:00', '23:59:59'),
('Parking', 'Secure free parking facilities with valet service available, covered parking spaces, and 24-hour security surveillance', '00:00:00', '23:59:59'),
('Room Service', 'Comprehensive 24/7 room service offering full menu, breakfast in bed, and amenities delivered directly to your room', '00:00:00', '23:59:59'),
('Business Center', 'Professional business facilities with meeting rooms, conference halls, printers, fax, and administrative support', '07:00:00', '22:00:00'),
('Kids Club', 'Supervised children play area with educational games, activities, crafts, and entertainment by trained staff', '09:00:00', '20:00:00'),
('Concierge', 'Expert concierge service for tour bookings, restaurant reservations, transportation, and local recommendations', '08:00:00', '20:00:00'),
('Laundry Service', 'Professional laundry and dry cleaning service with same-day delivery available for hotel guests convenience', '08:00:00', '20:00:00');

-- Insert Attractions (tourist attractions and activities)
INSERT INTO entity_attraction (name, description, people_capacity, open_at, close_at) VALUES
('Tango Shows', 'Authentic Buenos Aires tango performances with professional dancers, live orchestra, traditional music, and optional dinner packages', 150, '20:00:00', '23:30:00'),
('Teatro Colón Tour', 'Guided tours of the historic grand theater featuring opera house architecture, backstage access, and cultural history', 80, '10:00:00', '18:00:00'),
('La Boca Walk', 'Walking tour through vibrant neighborhood with colorful Caminito street, tango dancers, street art, and local museums', 200, '10:00:00', '18:00:00'),
('San Telmo Market', 'Sunday antique market in colonial neighborhood featuring local artisans, vintage items, street performers, and crafts', 500, '10:00:00', '17:00:00'),
('Recoleta Cemetery', 'Historic cemetery tour visiting ornate mausoleums, famous graves including Eva Perón, and architectural masterpieces', 100, '08:00:00', '18:00:00'),
('Palermo Parks', 'Beautiful parks and botanical gardens perfect for picnics, jogging, cultural events, and outdoor relaxation activities', 1000, '08:00:00', '20:00:00'),
('Wine Tasting', 'Guided tours to Mendoza wineries with wine tasting sessions, vineyard walks, and premium wine sampling experiences', 30, '10:00:00', '18:00:00'),
('Mountain Trekking', 'Adventure trekking in Andes mountains with experienced guides, equipment provided, and spectacular nature views', 20, '08:00:00', '17:00:00'),
('City Bike Tours', 'Bicycle tours exploring city neighborhoods, historical sites, parks, and hidden gems with bilingual guides', 25, '09:00:00', '13:00:00'),
('Cooking Classes', 'Interactive Argentine cooking classes learning to prepare empanadas, asado, and traditional desserts with chef', 15, '15:00:00', '19:00:00');

-- Associate Benefits with Hotels (Many-to-Many)
-- Luxury Palace Hotel (5 stars) - All premium benefits
INSERT INTO entity_hotel_benefit (entity_hotel_id, entity_benefit_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11), (1, 12);

-- Executive Business Hotel (4 stars) - Business-focused benefits
INSERT INTO entity_hotel_benefit (entity_hotel_id, entity_benefit_id) VALUES
(2, 3), (2, 4), (2, 6), (2, 7), (2, 8), (2, 9), (2, 11), (2, 12);

-- Comfort Inn Express (3.5 stars) - Standard benefits
INSERT INTO entity_hotel_benefit (entity_hotel_id, entity_benefit_id) VALUES
(3, 1), (3, 4), (3, 6), (3, 7), (3, 8);

-- Budget Plaza Hotel (2.5 stars) - Basic benefits
INSERT INTO entity_hotel_benefit (entity_hotel_id, entity_benefit_id) VALUES
(4, 4), (4, 6), (4, 7);

-- Eco Resort Mountain (4.5 stars) - Nature-focused benefits
INSERT INTO entity_hotel_benefit (entity_hotel_id, entity_benefit_id) VALUES
(5, 1), (5, 2), (5, 3), (5, 4), (5, 6), (5, 7), (5, 11);

-- Associate Attractions with Hotels (Many-to-Many)
-- Luxury Palace Hotel - City attractions
INSERT INTO entity_hotel_attraction (entity_hotel_id, entity_attraction_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 9), (1, 10);

-- Executive Business Hotel - Business district attractions
INSERT INTO entity_hotel_attraction (entity_hotel_id, entity_attraction_id) VALUES
(2, 1), (2, 2), (2, 6), (2, 9);

-- Comfort Inn Express - Popular tourist spots
INSERT INTO entity_hotel_attraction (entity_hotel_id, entity_attraction_id) VALUES
(3, 3), (3, 4), (3, 5), (3, 6), (3, 9);

-- Budget Plaza Hotel - Affordable attractions
INSERT INTO entity_hotel_attraction (entity_hotel_id, entity_attraction_id) VALUES
(4, 3), (4, 4), (4, 6);

-- Eco Resort Mountain - Nature activities
INSERT INTO entity_hotel_attraction (entity_hotel_id, entity_attraction_id) VALUES
(5, 7), (5, 8), (5, 9);

-- Insert Rooms for each hotel
-- Luxury Palace Hotel (50 rooms) - Mix of room types
INSERT INTO entity_room (room_type, bed_type, people_capacity, number_of_beds, floor, price_per_night, state, hotel_id, times_booked) VALUES
-- Floor 1
('STANDARD', 'DOUBLE', 2, 1, 1, 150.00, 'FREE', 1, 5),
('STANDARD', 'DOUBLE', 2, 1, 1, 150.00, 'FREE', 1, 3),
('DELUXE', 'QUEEN', 2, 1, 1, 200.00, 'FREE', 1, 8),
('DELUXE', 'QUEEN', 2, 1, 1, 200.00, 'FREE', 1, 6),
-- Floor 2
('SUITE', 'KING', 2, 1, 2, 300.00, 'RESERVED', 1, 12),
('SUITE', 'KING', 2, 1, 2, 300.00, 'FREE', 1, 10),
('EXECUTIVE', 'KING', 3, 2, 2, 350.00, 'FREE', 1, 15),
('EXECUTIVE', 'KING', 3, 2, 2, 350.00, 'FREE', 1, 14),
-- Floor 3
('PRESIDENTIAL', 'KING', 4, 2, 3, 500.00, 'FREE', 1, 20),
('PRESIDENTIAL', 'KING', 4, 2, 3, 500.00, 'FREE', 1, 18);

-- Executive Business Hotel (80 rooms)
INSERT INTO entity_room (room_type, bed_type, people_capacity, number_of_beds, floor, price_per_night, state, hotel_id, times_booked) VALUES
('STANDARD', 'SINGLE', 1, 1, 1, 100.00, 'FREE', 2, 2),
('STANDARD', 'DOUBLE', 2, 1, 1, 120.00, 'FREE', 2, 4),
('DELUXE', 'QUEEN', 2, 1, 2, 150.00, 'RESERVED', 2, 7),
('EXECUTIVE', 'KING', 2, 1, 3, 200.00, 'FREE', 2, 10),
('SUITE', 'KING', 3, 2, 4, 250.00, 'FREE', 2, 12);

-- Comfort Inn Express (120 rooms)
INSERT INTO entity_room (room_type, bed_type, people_capacity, number_of_beds, floor, price_per_night, state, hotel_id, times_booked) VALUES
('STANDARD', 'SINGLE', 1, 1, 1, 80.00, 'FREE', 3, 1),
('STANDARD', 'DOUBLE', 2, 1, 1, 100.00, 'FREE', 3, 2),
('STANDARD', 'TWIN', 2, 2, 2, 110.00, 'FREE', 3, 3),
('DELUXE', 'QUEEN', 2, 1, 2, 130.00, 'FREE', 3, 5),
('DELUXE', 'KING', 3, 2, 3, 160.00, 'FREE', 3, 4);

-- Budget Plaza Hotel (100 rooms)
INSERT INTO entity_room (room_type, bed_type, people_capacity, number_of_beds, floor, price_per_night, state, hotel_id, times_booked) VALUES
('STANDARD', 'SINGLE', 1, 1, 1, 60.00, 'FREE', 4, 0),
('STANDARD', 'DOUBLE', 2, 1, 1, 80.00, 'FREE', 4, 1),
('STANDARD', 'TWIN', 2, 2, 2, 85.00, 'FREE', 4, 2),
('DELUXE', 'DOUBLE', 2, 1, 2, 100.00, 'FREE', 4, 3);

-- Eco Resort Mountain (40 rooms)
INSERT INTO entity_room (room_type, bed_type, people_capacity, number_of_beds, floor, price_per_night, state, hotel_id, times_booked) VALUES
('STANDARD', 'QUEEN', 2, 1, 1, 180.00, 'FREE', 5, 6),
('DELUXE', 'KING', 2, 1, 1, 220.00, 'FREE', 5, 8),
('SUITE', 'KING', 3, 2, 2, 300.00, 'FREE', 5, 10),
('EXECUTIVE', 'KING', 4, 2, 2, 350.00, 'FREE', 5, 12);

-- Insert sample Persons (guests)
INSERT INTO entity_person (name, dni, email, age, cell_phone_number, number_of_reservations) VALUES
('Juan Carlos Pérez', '30123456', 'juan.perez@email.com', 35, '+5491134567890', 0),
('María Fernanda García', '28654321', 'maria.garcia@email.com', 42, '+5491145678901', 0),
('Roberto Luis Martínez', '32987654', 'roberto.martinez@email.com', 28, '+5491156789012', 0),
('Ana Paula Rodríguez', '29456789', 'ana.rodriguez@email.com', 31, '+5491167890123', 0),
('Carlos Eduardo López', '33321654', 'carlos.lopez@email.com', 45, '+5491178901234', 0),
('Laura Beatriz Fernández', '31789456', 'laura.fernandez@email.com', 38, '+5491189012345', 0),
('Diego Alejandro González', '27852963', 'diego.gonzalez@email.com', 26, '+5491190123456', 0),
('Patricia Mónica Silva', '34159753', 'patricia.silva@email.com', 52, '+5491101234567', 0);

-- Insert Person Addresses
INSERT INTO entity_address (street, number, floor, door_number, state_code, address_type) VALUES
('Avenida Santa Fe', '1500', '8', 'B', 'AR-C', 'PERSON'),
('Calle Belgrano', '2300', '3', 'A', 'AR-B', 'PERSON'),
('Avenida Córdoba', '4500', '12', 'C', 'AR-C', 'PERSON'),
('Calle San Martín', '890', '5', 'D', 'AR-S', 'PERSON'),
('Avenida Libertador', '6700', '15', 'A', 'AR-C', 'PERSON'),
('Calle Moreno', '1200', '2', 'B', 'AR-X', 'PERSON'),
('Avenida Rivadavia', '3400', '7', 'C', 'AR-B', 'PERSON'),
('Calle Sarmiento', '5600', '4', 'A', 'AR-M', 'PERSON');

-- Link Person to Address
UPDATE entity_person SET address_id = 6 WHERE id = 1;
UPDATE entity_person SET address_id = 7 WHERE id = 2;
UPDATE entity_person SET address_id = 8 WHERE id = 3;
UPDATE entity_person SET address_id = 9 WHERE id = 4;
UPDATE entity_person SET address_id = 10 WHERE id = 5;
UPDATE entity_person SET address_id = 11 WHERE id = 6;
UPDATE entity_person SET address_id = 12 WHERE id = 7;
UPDATE entity_person SET address_id = 13 WHERE id = 8;

-- Insert sample Reservations
INSERT INTO entity_reservation (person_id, room_id, start_at, end_at, number_of_people, number_of_nights, total_price) VALUES
-- Reservation 1: Luxury Palace Hotel - Suite
(1, 5, '2025-12-20', '2025-12-25', 2, 5, 1500.00),
-- Reservation 2: Executive Business Hotel - Deluxe
(2, 13, '2025-12-15', '2025-12-18', 2, 3, 450.00);

-- Update person reservation counts
UPDATE entity_person SET number_of_reservations = 1 WHERE id = 1;
UPDATE entity_person SET number_of_reservations = 1 WHERE id = 2;

-- Insert Room Booking Periods for the reservations
INSERT INTO entity_room_booking_period (room_id, reservation_id, start_at, end_at, status) VALUES
(5, 1, '2025-12-20', '2025-12-25', 'RESERVED'),
(13, 2, '2025-12-15', '2025-12-18', 'RESERVED');
