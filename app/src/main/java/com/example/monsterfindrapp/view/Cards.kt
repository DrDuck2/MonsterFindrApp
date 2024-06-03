package com.example.monsterfindrapp.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BackHand
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Start
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import com.example.monsterfindrapp.utility.AuthenticationManager
import com.example.monsterfindrapp.R
import com.example.monsterfindrapp.utility.RequestsRepository
import com.example.monsterfindrapp.utility.UsersRepository
import com.example.monsterfindrapp.model.Locations
import com.example.monsterfindrapp.model.MonsterItem
import com.example.monsterfindrapp.model.RequestLocations
import com.example.monsterfindrapp.model.RequestUser
import com.example.monsterfindrapp.model.StoreItem
import com.example.monsterfindrapp.model.User
import com.example.monsterfindrapp.viewModel.HandleNotificationViewModel
import com.example.monsterfindrapp.viewModel.ItemsViewModel
import com.example.monsterfindrapp.viewModel.StoreItemsViewModel
import com.example.monsterfindrapp.viewModel.UsersViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun StoreCard(location: Locations,viewModel: StoreItemsViewModel, onClick: (Locations) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    val latitude = location.location.latitude
    val roundedLatitude = BigDecimal(latitude).setScale(4, RoundingMode.HALF_UP).toDouble()
    val longitude =  location.location.longitude
    val roundedLongitude= BigDecimal(longitude).setScale(4, RoundingMode.HALF_UP).toDouble()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = { onClick(location) })
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Store: " + location.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.body1.fontSize
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Latitude: $roundedLatitude",
                    style = MaterialTheme.typography.body2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Longitude: $roundedLongitude",
                    style = MaterialTheme.typography.body2
                )
            }
            Button(
                onClick = {
                    showDialog = true
                },
                modifier = Modifier
                    .size(60.dp, 60.dp)
                    .clip(RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Red,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = "Remove Store",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                androidx.compose.material.Text("Remove Store")
            },
            text = {
                Column {
                    Text("Are you sure you want to remove this store?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("(You can't undo this action)")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeStore(location)
                        showDialog = false // Dismiss dialog after confirmation
                    }
                ) {
                    androidx.compose.material.Text("Yes")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false }
                ) {
                    androidx.compose.material.Text("Cancel")
                }
            }
        )
    }
}
@Composable
fun StoreItemCard(item: StoreItem, viewModel: StoreItemsViewModel){
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(item.monsterItem.imageUrl)
                            .crossfade(true)
                            .scale(Scale.FILL)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .build()
                    ),
                    contentDescription = "Item Image",
                    modifier = Modifier
                        .size(100.dp)
                        .aspectRatio(1f)
                )
                Column{
                    Text(text = item.monsterItem.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = "Price: " +item.price.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = "Availability: " +item.availability,
                        fontSize = 10.sp)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = item.monsterItem.description,
                        fontSize = 10.sp)
                    Spacer(modifier = Modifier.padding(20.dp))
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    Text(text = "Last Update: " + dateFormat.format(item.lastUpdated).toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp)
                }
            }
            Button(
                onClick = {
                    showDialog = true
                },
                modifier = Modifier
                    .size(60.dp, 60.dp)
                    .clip(RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Red,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = "Remove Item",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                androidx.compose.material.Text("Remove Item")
            },
            text = {
                Column{
                    Text("Are you sure you want to remove this item?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("(This action can't be reversed)")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeStoreItem(item)
                        showDialog = false // Dismiss dialog after confirmation
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun LocationCard(item: StoreItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ){
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(item.monsterItem.imageUrl)
                            .crossfade(true)
                            .scale(Scale.FILL)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .build()
                    ),
                    contentDescription = "Item Image",
                    modifier = Modifier
                        .size(100.dp)
                        .aspectRatio(1f)
                )
                Spacer(modifier = Modifier.padding(8.dp))
                Column{
                    Text(text = "Name: " +item.monsterItem.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = "Price: " +item.price.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = "Availability: " +item.availability,
                        fontSize = 10.sp)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = item.monsterItem.description,
                        fontSize = 10.sp)
                    Spacer(modifier = Modifier.padding(20.dp))
                    Text(text = "Last Update: " +item.lastUpdated.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notification: com.example.monsterfindrapp.model.Notification, viewModel: HandleNotificationViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    androidx.compose.material.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(notification.item.imageUrl)
                            .crossfade(true)
                            .scale(Scale.FILL)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .build()
                    ),
                    contentDescription = "Item Image",
                    modifier = Modifier
                        .size(50.dp)
                        .aspectRatio(1f)
                )
                Spacer(modifier = Modifier.padding(16.dp))
                Column {
                    androidx.compose.material.Text(
                        text = notification.item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
            Spacer(modifier = Modifier.padding(8.dp))
            Button(
                onClick = {
                    showDialog = true
                },
                modifier = Modifier
                    .size(60.dp, 30.dp)
                    .clip(RoundedCornerShape(8.dp)),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Red,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = "Remove Notification",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                androidx.compose.material.Text("Remove Notification")
            },
            text = {
                androidx.compose.material.Text("Are you sure you want to remove the notification?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeNotification(notification)
                        showDialog = false // Dismiss dialog after confirmation
                    }
                ) {
                    androidx.compose.material.Text("Yes")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false }
                ) {
                    androidx.compose.material.Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ItemCard(item: MonsterItem, viewModel: ItemsViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp),
            ){
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(item.imageUrl)
                            .crossfade(true)
                            .scale(Scale.FILL)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .build()
                    ),
                    contentDescription = "Item Image",
                    modifier = Modifier
                        .size(50.dp)
                        .aspectRatio(1f)
                )
                Spacer(modifier = Modifier.padding(16.dp))
                Column{
                    Text(text = item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = item.description)
                }
            }
            Button(
                onClick = {
                    showDialog = true
                },
                modifier = Modifier
                    .size(60.dp, 50.dp)
                    .clip(RoundedCornerShape(8.dp)),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Red,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = "Remove Item",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                androidx.compose.material.Text("Remove Item")
            },
            text = {
                androidx.compose.material.Text("Are you sure you want to remove item?")
            },
            confirmButton = {
                androidx.compose.material.Button(
                    onClick = {
                        viewModel.removeItem(item)
                        showDialog = false // Dismiss dialog after confirmation
                    }
                ) {
                    androidx.compose.material.Text("Yes")
                }
            },
            dismissButton = {
                androidx.compose.material.Button(
                    onClick = { showDialog = false }
                ) {
                    androidx.compose.material.Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun RequestUserCard(request: RequestUser, onCardClick: () -> Unit) {
    androidx.compose.material.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        backgroundColor = UsersRepository.getUserColor(request.userInfo)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { onCardClick() },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                androidx.compose.material.Text(
                    text = "Email: " + request.userInfo.email,
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.body1.fontSize
                )
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material.Text(text = "Uid: " + request.userInfo.uid)
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (request.userInfo.isAdmin) {
                if (request.userInfo.uid == AuthenticationManager.getCurrentUserId()) {
                    Text(
                        text = "ADMIN (YOU)",
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.body1.fontSize
                    )

                } else {
                    Text(
                        text = "ADMIN", fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.body1.fontSize
                    )
                }
            }
        }
    }
}

@Composable
fun RequestsCard(request: RequestLocations, onCardClick: () -> Unit) {
    androidx.compose.material.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        backgroundColor = RequestsRepository.getRequestColor(request)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { onCardClick() },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Store: " + request.id,
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.body1.fontSize
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Item: " + request.item
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Price: " + request.price.toString()
                )
            }
        }
    }
}

@Composable
fun UserCard(user: User, viewModel: UsersViewModel) {

    val suspendDate by viewModel.getSuspendDate(user).collectAsState()

    var userWork by remember{ mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }


    androidx.compose.material.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        backgroundColor = UsersRepository.getUserColor(user)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                androidx.compose.material.Text(
                    text = "Email: " + user.email,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Uid: " + user.uid, fontSize = 8.sp)
                Spacer(modifier = Modifier.height(16.dp))
                if (user.isSuspended) {
                    androidx.compose.material.Text(
                        text = "Suspend Date: ${
                            suspendDate?.let {
                                DateFormat.getDateInstance().format(
                                    it
                                )
                            }
                        }"
                    )
                }
            }
            if (!user.isAdmin) {
                if (!user.isSuspended) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Button(
                            onClick = {
                                userWork = "Suspend User"
                                showDialog = true
                            },
                            modifier = Modifier
                                .size(60.dp, 50.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.Gray,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Stop,
                                contentDescription = "Suspend User",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                userWork = "Ban User"
                                showDialog = true
                            },
                            modifier = Modifier
                                .size(60.dp, 50.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.Red,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.BackHand,
                                contentDescription = "Ban User",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .padding(8.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Button(
                            onClick = {
                                userWork = "Un Suspend User"
                                showDialog = true
                            },
                            modifier = Modifier
                                .size(60.dp, 50.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.Gray,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Start,
                                contentDescription = "Manual Un Suspend",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                userWork = "Ban User"
                                showDialog = true
                            },
                            modifier = Modifier
                                .size(60.dp, 50.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.Red,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.BackHand,
                                contentDescription = "Ban User",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

            } else {
                if (user.uid == AuthenticationManager.getCurrentUserId()) {
                    androidx.compose.material.Text(
                        text = "ADMIN (YOU)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                } else {
                    androidx.compose.material.Text(
                        text = "ADMIN", fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
    if(showDialog){
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                androidx.compose.material.Text(userWork)
            },
            text = {
                Column{
                    androidx.compose.material.Text("Are you sure you want to: $userWork")
                    if(userWork == "Suspend User"){
                        Spacer(modifier = Modifier.height(8.dp))
                        androidx.compose.material.Text("(User will be Suspended for one week)")
                    }else if(userWork == "Ban User"){
                        Spacer(modifier = Modifier.height(8.dp))
                        androidx.compose.material.Text("(You won't be able to revert this action")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (userWork) {
                            "Suspend User" -> viewModel.callSuspendUser(user)
                            "Ban User" -> viewModel.callBanUser(user)
                            "Un Suspend User" -> viewModel.callUnSuspendUser(user)
                        }
                        showDialog = false // Dismiss dialog after confirmation
                    }
                ) {
                    androidx.compose.material.Text("Yes")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false }
                ) {
                    androidx.compose.material.Text("Cancel")
                }
            }
        )
    }
}