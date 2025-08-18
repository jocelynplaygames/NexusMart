import { Cloudinary } from 'cloudinary-core';

const cloudinary = new Cloudinary({ cloud_name: 'dl7atizzb', secure: true });

const uploadFile = async (file) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('upload_preset', 'NexusMart_UserAvatar'); // You need to set up an upload preset in Cloudinary

    const response = await fetch(`https://api.cloudinary.com/v1_1/dl7atizzb/image/upload`, {
        method: 'POST',
        body: formData
    });

    const data = await response.json();
    return data.secure_url;
};

export default { uploadFile };
