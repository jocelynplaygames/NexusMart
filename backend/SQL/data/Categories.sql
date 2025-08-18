-- File: backend/SQL/data/Categories.sql
-- Insert category data
INSERT INTO main_categories (name) VALUES
('Electronics'),
('Clothing'),
('Home & Kitchen'),
('Books'),
('Toys & Games');

INSERT INTO sub_categories (id, main_category_id, name, created_at, updated_at, image_url)
VALUES
(1, 1, 'Mobile Phones', '2024-05-27 18:01:01', '2024-05-27 18:01:00', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1716847260/NexusMart/Category/kfene1d9okq1i5pdyeqb.png'),
(2, 1, 'Laptops', '2024-05-27 18:10:22', '2024-05-27 18:10:22', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1716847822/NexusMart/Category/fmsfaosux4ooxwqqr4vp.png'),
(3, 1, 'Cameras', '2024-05-27 18:11:38', '2024-05-27 18:11:37', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1716847897/NexusMart/Category/jdbphzgwxm5x1kpzflc4.png'),
(4, 1, 'Televisions', '2024-05-27 18:12:36', '2024-05-27 18:12:36', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1716847955/NexusMart/Category/kiobkw2psnvi1cak7uyf.png'),
(5, 2, 'Men\'s Clothing', '2024-05-27 18:13:50', '2024-05-27 18:13:49', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1716848029/NexusMart/Category/esvvmydj2nq7zrm0uuor.png'),
(6, 2, 'Women\'s Clothing', '2024-05-27 18:15:56', '2024-05-27 18:15:56', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1716848155/NexusMart/Category/rbngsjolfx0f34jl0hh1.png'),
(7, 2, 'Kid\'s Clothing', '2024-05-27 18:16:34', '2024-05-27 18:16:34', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1716848194/NexusMart/Category/guq7yvxstpvh7p4j8wk8.png'),
(8, 3, 'Kitchen Appliances', '2024-05-27 18:17:43', '2024-05-27 18:17:42', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1716848262/NexusMart/Category/tzeyni6bmvgep6dyvxwr.png'),
(9, 3, 'Home Decor', '2024-05-27 18:18:29', '2024-05-27 18:18:28', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1716848308/NexusMart/Category/nbtrle02ah6rf84lpehd.png'),
(10, 3, 'Furniture', '2024-05-27 18:18:57', '2024-05-27 18:18:57', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1716848337/NexusMart/Category/kgk2tmiaegg669fbc8lz.png'),
(11, 4, 'Fiction', '2024-05-27 18:19:33', '2024-05-27 18:19:33', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1716848373/NexusMart/Category/x9um1mxctrjj8osxjwyo.png'),
(12, 4, 'Education', '2024-05-27 18:20:04', '2024-05-27 18:20:03', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1716848403/NexusMart/Category/evluyv8y5kdtd36aa9yz.png'),
(13, 4, 'Children\'s', '2024-05-27 18:20:49', '2024-05-27 19:01:42', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1716850901/NexusMart/Category/utofnnbgzjzotwauyhrh.png'),
(14, 5, 'Board Games', '2024-05-27 18:21:22', '2024-05-27 18:21:22', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1716848482/NexusMart/Category/fedkphtcykmvdg5qremp.png'),
(15, 5, 'Puzzles', '2024-05-27 18:21:55', '2024-05-27 18:21:55', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1716848515/NexusMart/Category/d5ufoghwl6cya8t9gal0.png'),
(16, 5, 'Outdoor Toys', '2024-05-27 18:22:45', '2024-05-27 18:22:44', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1716848564/NexusMart/Category/ip9cet3ngltcwwk3v5ga.png'),

(17, 1, 'Headphones', '2024-05-28 09:00:00', '2024-05-28 09:00:00', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1717279791/NexusMart/Category/izsqrtb0fzjzhblyfzfn.png'),
(18, 1, 'Smart Watches', '2024-05-28 09:10:00', '2024-05-28 09:10:00', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1717279909/NexusMart/Category/cyew2qu4oghixexsl0ub.png'),
(19, 1, 'Tablets', '2024-05-28 09:20:00', '2024-05-28 09:20:00', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1717279971/NexusMart/Category/vpkqqonfqcdhg8nxrpod.png'),
(20, 1, 'Gaming Consoles', '2024-05-28 09:30:00', '2024-05-28 09:30:00', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1717279997/NexusMart/Category/rov1zgj9tepwpdsbyduo.png'),
(21, 2, 'Shoes', '2024-05-28 09:40:00', '2024-05-28 09:40:00', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1717280063/NexusMart/Category/en95r3o9h8jz6vvxrm9m.png'),
(22, 2, 'Accessories', '2024-05-28 09:50:00', '2024-05-28 09:50:00', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1717280100/NexusMart/Category/vhuwnts15xogakfehg6h.png'),
(23, 2, 'Sportswear', '2024-05-28 10:00:00', '2024-05-28 10:00:00', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1717280139/NexusMart/Category/eqvgwjnxgj5s5lawgkps.png'),
(24, 2, 'Outerwear', '2024-05-28 10:10:00', '2024-05-28 10:10:00', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1717280182/NexusMart/Category/phvqau3eumf7bvimvgfn.png'),
(25, 3, 'Bedding', '2024-05-28 10:20:00', '2024-05-28 10:20:00', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1717280227/NexusMart/Category/vghhuuqi6gv4oo0k0nmb.png'),
(26, 3, 'Cookware', '2024-05-28 10:30:00', '2024-05-28 10:30:00', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1717280278/NexusMart/Category/ch7laxpv1wuz2odkbuef.png'),
(27, 3, 'Lighting', '2024-05-28 10:40:00', '2024-05-28 10:40:00', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1717280328/NexusMart/Category/uw9itoejlkk3bxvmia3n.png'),
(28, 3, 'Storage Solutions', '2024-05-28 10:50:00', '2024-05-28 10:50:00', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1717280361/NexusMart/Category/usq8khm1yed0useuikxw.png'),
(29, 4, 'Romantic', '2024-05-28 11:00:00', '2024-05-28 11:00:00', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1717283752/NexusMart/Category/dzbdlsg4qhp7yrxldvie.png'),
(30, 4, 'Biographies', '2024-05-28 11:10:00', '2024-05-28 11:10:00', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1717280463/NexusMart/Category/j5k7sjrxkxef6lzkcqbm.png'),
(31, 4, 'Science Fiction', '2024-05-28 11:20:00', '2024-05-28 11:20:00', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1717280496/NexusMart/Category/zpnj2fcc1kukf5sojlrc.png'),
(32, 4, 'Fantasy', '2024-05-28 11:30:00', '2024-05-28 11:30:00', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1717280534/NexusMart/Category/bvny2jsy5bopiizt1iea.png'),
(33, 5, 'Dolls & Action Figures', '2024-05-28 11:40:00', '2024-05-28 11:40:00', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1717280583/NexusMart/Category/feff7nj9nhflb751okni.png'),
(34, 5, 'Educational Toys', '2024-05-28 11:50:00', '2024-05-28 11:50:00', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1717280638/NexusMart/Category/mdstsdfnxpm6opqori7r.png'),
(35, 5, 'Remote Control Toys', '2024-05-28 12:00:00', '2024-05-28 12:00:00', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1717280684/NexusMart/Category/oavedo8o1pvcyrifekwk.png'),
(36, 5, 'Stuffed Animals', '2024-05-28 12:10:00', '2024-05-28 12:10:00', 'http://res.cloudinary.com/dl7atizzb/image/upload/v1717280721/NexusMart/Category/n7bmh1npynrdqjhbju41.png');

